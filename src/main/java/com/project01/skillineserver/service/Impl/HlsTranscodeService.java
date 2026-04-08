package com.project01.skillineserver.service.Impl;

import com.project01.skillineserver.entity.MediaAssetEntity;
import com.project01.skillineserver.entity.MediaAssetVariantEntity;
import com.project01.skillineserver.enums.PlaybackType;
import com.project01.skillineserver.enums.ProcessStatus;
import com.project01.skillineserver.enums.VariantType;
import com.project01.skillineserver.repository.MediaAssetRepository;
import com.project01.skillineserver.repository.MediaAssetVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class HlsTranscodeService {

    private final S3Client s3Client;
    private final MediaAssetRepository mediaAssetRepository;
    private final MediaAssetVariantRepository variantRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${app.ffmpeg.path:ffmpeg}")
    private String ffmpegPath;

    // Timeout transcode tối đa 2 tiếng cho video dài
    private static final long TRANSCODE_TIMEOUT_MINUTES = 120;

    /**
     * Entry point: download video từ S3 → transcode HLS → upload HLS lên S3.
     * Được gọi từ MediaProcessingConsumer khi nhận Kafka event.
     */
    public void transcode(MediaAssetEntity asset) throws Exception {

        String assetId = asset.getId();
        // Thư mục làm việc tạm thời: /tmp/skilline/{assetId}/
        Path workDir = Path.of(System.getProperty("java.io.tmpdir"), "skilline", assetId);

        try {
            Files.createDirectories(workDir);
            log.info("[{}] Work dir created: {}", assetId, workDir);

            // === Bước 1: Download video gốc từ S3 về local ===
            Path inputFile = downloadFromS3(asset.getBucket(), asset.getObjectKey(), workDir);
            log.info("[{}] Downloaded input: {} ({} bytes)",
                    assetId, inputFile.getFileName(), Files.size(inputFile));

            // === Bước 2: Chạy FFmpeg để tạo HLS adaptive bitrate ===
            Path hlsOutputDir = workDir.resolve("hls");
            Files.createDirectories(hlsOutputDir);
            runFfmpeg(assetId, inputFile, hlsOutputDir);
            log.info("[{}] FFmpeg transcode completed", assetId);

            // === Bước 3: Upload toàn bộ HLS output lên S3 ===
            // videos/raw/2025/06/uuid.mp4 → videos/hls/2025/06/uuid/
            String hlsS3BasePath = asset.getObjectKey()
                    .replace("videos/raw/", "videos/hls/")
                    .replaceAll("\\.[^.]+$", ""); // bỏ .mp4

            uploadHlsToS3(hlsOutputDir, hlsS3BasePath);
            log.info("[{}] HLS uploaded to S3 under: {}", assetId, hlsS3BasePath);

            // === Bước 4: Lưu variants vào DB + cập nhật asset ===
            saveVariants(assetId, hlsS3BasePath);
            String masterKey = hlsS3BasePath + "/master.m3u8";
            asset.setHlsMasterKey(masterKey);
            asset.setPlaybackType(PlaybackType.HLS);
            asset.setProcessStatus(ProcessStatus.COMPLETED);
            mediaAssetRepository.save(asset);
            log.info("[{}] Done. HLS master: {}", assetId, masterKey);

        } finally {
            // Luôn xóa thư mục tạm dù thành công hay thất bại
            deleteDirectory(workDir);
            log.info("[{}] Cleaned up work dir", assetId);
        }
    }

    // =====================================================================
    // BƯỚC 1 — Download từ S3
    // =====================================================================

    private Path downloadFromS3(String s3Bucket, String objectKey, Path workDir) {
        // Lấy tên file gốc từ objectKey: videos/raw/2025/06/uuid.mp4 → uuid.mp4
        String fileName = objectKey.substring(objectKey.lastIndexOf('/') + 1);
        Path localFile = workDir.resolve(fileName);

        log.info("Downloading s3://{}/{} → {}", s3Bucket, objectKey, localFile);

        s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(s3Bucket)
                        .key(objectKey)
                        .build(),
                localFile   // AWS SDK v2 tự stream thẳng vào file
        );

        return localFile;
    }

    // =====================================================================
    // BƯỚC 2 — Chạy FFmpeg
    // =====================================================================

    /**
     * Tạo adaptive HLS với 3 resolution: 360p, 480p, 720p.
     * <p>
     * Cấu trúc output trong hlsOutputDir:
     * hls/
     * ├── master.m3u8          ← playlist chính, player đọc cái này
     * ├── 360p.m3u8
     * ├── 480p.m3u8
     * ├── 720p.m3u8
     * ├── 360p_000.ts
     * ├── 360p_001.ts
     * ├── 480p_000.ts
     * └── ...
     * <p>
     * Lệnh FFmpeg giải thích:
     * -i input.mp4                   → file đầu vào
     * -filter_complex [v]split=3...  → tách video stream thành 3 luồng song song
     * -map [v1] -map 0:a             → map video 360p + audio gốc
     * -c:v libx264                   → encode H.264 (tương thích nhất)
     * -c:a aac                       → encode audio AAC
     * -hls_time 6                    → mỗi segment dài 6 giây
     * -hls_list_size 0               → giữ tất cả segment trong playlist
     * -hls_segment_type mpegts       → format .ts
     * -hls_segment_filename          → pattern tên file segment
     */
    private void runFfmpeg(String assetId, Path inputFile, Path hlsOutputDir) throws Exception {

        String input = inputFile.toAbsolutePath().toString();
        String outDir = hlsOutputDir.toAbsolutePath().toString();

        // Tạo master.m3u8 thủ công (FFmpeg không tự tạo adaptive master)
        // FFmpeg sẽ tạo 360p.m3u8, 480p.m3u8, 720p.m3u8
        // Sau đó ta ghép thủ công thành master.m3u8

        List<String> cmd = List.of(
                ffmpegPath,
                "-i", input,
                "-filter_complex",
                "[0:v]split=3[v1][v2][v3];" +
                        "[v1]scale=640:360[s1];" +
                        "[v2]scale=854:480[s2];" +
                        "[v3]scale=1280:720[s3]",

                // --- 360p ---
                "-map", "[s1]", "-map", "0:a",
                "-c:v", "libx264", "-b:v", "800k",
                "-c:a", "aac", "-b:a", "96k",
                "-hls_time", "6",
                "-hls_list_size", "0",
                "-hls_segment_type", "mpegts",
                "-hls_segment_filename", outDir + "/360p_%03d.ts",
                outDir + "/360p.m3u8",

                // --- 480p ---
                "-map", "[s2]", "-map", "0:a",
                "-c:v", "libx264", "-b:v", "1400k",
                "-c:a", "aac", "-b:a", "128k",
                "-hls_time", "6",
                "-hls_list_size", "0",
                "-hls_segment_type", "mpegts",
                "-hls_segment_filename", outDir + "/480p_%03d.ts",
                outDir + "/480p.m3u8",

                // --- 720p ---
                "-map", "[s3]", "-map", "0:a",
                "-c:v", "libx264", "-b:v", "2800k",
                "-c:a", "aac", "-b:a", "128k",
                "-hls_time", "6",
                "-hls_list_size", "0",
                "-hls_segment_type", "mpegts",
                "-hls_segment_filename", outDir + "/720p_%03d.ts",
                outDir + "/720p.m3u8",

                "-y" // overwrite nếu đã tồn tại
        );

        log.info("[{}] Running FFmpeg command:\n{}", assetId, String.join(" ", cmd));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true); // merge stderr vào stdout để đọc 1 stream
        pb.directory(hlsOutputDir.toFile());

        Process process = pb.start();

        // Đọc log FFmpeg để debug (chạy trên thread riêng để không block)
        Thread logThread = new Thread(() -> {
            try (var reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Log ở level DEBUG để không spam console
                    log.debug("[ffmpeg][{}] {}", assetId, line);
                }
            } catch (IOException ignored) {
            }
        });
        logThread.setDaemon(true);
        logThread.start();

        boolean finished = process.waitFor(TRANSCODE_TIMEOUT_MINUTES, TimeUnit.MINUTES);

        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException(
                    "FFmpeg timeout after " + TRANSCODE_TIMEOUT_MINUTES + " minutes for asset: " + assetId);
        }

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            throw new RuntimeException(
                    "FFmpeg failed with exit code " + exitCode + " for asset: " + assetId);
        }

        // Tạo master.m3u8 sau khi FFmpeg chạy xong
        createMasterPlaylist(hlsOutputDir);
    }

    /**
     * Tạo master.m3u8 — file HLS player đọc đầu tiên.
     * Player tự chọn resolution phù hợp với bandwidth.
     * <p>
     * Nội dung file:
     * #EXTM3U
     * #EXT-X-VERSION:3
     * #EXT-X-STREAM-INF:BANDWIDTH=800000,RESOLUTION=640x360
     * 360p.m3u8
     * ...
     */
    private void createMasterPlaylist(Path hlsOutputDir) throws IOException {
        StringBuilder master = new StringBuilder();
        master.append("#EXTM3U\n");
        master.append("#EXT-X-VERSION:3\n\n");

        master.append("#EXT-X-STREAM-INF:BANDWIDTH=896000,RESOLUTION=640x360,CODECS=\"avc1.42e01e,mp4a.40.2\"\n");
        master.append("360p.m3u8\n\n");

        master.append("#EXT-X-STREAM-INF:BANDWIDTH=1528000,RESOLUTION=854x480,CODECS=\"avc1.42e01e,mp4a.40.2\"\n");
        master.append("480p.m3u8\n\n");

        master.append("#EXT-X-STREAM-INF:BANDWIDTH=2928000,RESOLUTION=1280x720,CODECS=\"avc1.42e01e,mp4a.40.2\"\n");
        master.append("720p.m3u8\n\n");

        Path masterFile = hlsOutputDir.resolve("master.m3u8");
        Files.writeString(masterFile, master.toString());
        log.info("Created master.m3u8 at: {}", masterFile);
    }

    // =====================================================================
    // BƯỚC 3 — Upload HLS lên S3
    // =====================================================================

    /**
     * Walk toàn bộ thư mục hls/ và upload từng file lên S3.
     * <p>
     * Ví dụ:
     * local: /tmp/skilline/{uuid}/hls/master.m3u8
     * → s3:  videos/hls/2025/06/{uuid}/master.m3u8
     */
    private void uploadHlsToS3(Path hlsOutputDir, String s3BasePath) throws IOException {
        List<Path> files = new ArrayList<>();

        // Thu thập tất cả file trong thư mục hls/
        Files.walkFileTree(hlsOutputDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                files.add(file);
                return FileVisitResult.CONTINUE;
            }
        });

        log.info("Uploading {} HLS files to S3...", files.size());

        for (Path file : files) {
            // Tính relative path từ hlsOutputDir
            // /tmp/skilline/{uuid}/hls/360p_000.ts → 360p_000.ts
            String relativePath = hlsOutputDir.relativize(file).toString()
                    .replace("\\", "/"); // Windows path separator fix

            String s3Key = s3BasePath + "/" + relativePath;
            String contentType = resolveContentType(file.getFileName().toString());

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(s3Key)
                            .contentType(contentType)
                            // Cho phép CloudFront cache .ts 1 năm, .m3u8 không cache
                            .cacheControl(file.toString().endsWith(".ts")
                                    ? "max-age=31536000, immutable"
                                    : "no-cache, no-store")
                            .build(),
                    RequestBody.fromFile(file)
            );

            log.debug("Uploaded: {} → s3://{}/{}", relativePath, bucket, s3Key);
        }

        log.info("All {} files uploaded successfully", files.size());
    }

    private String resolveContentType(String fileName) {
        if (fileName.endsWith(".m3u8")) return "application/x-mpegURL";
        if (fileName.endsWith(".ts")) return "video/MP2T";
        return "application/octet-stream";
    }

    // =====================================================================
    // BƯỚC 4 — Lưu variants vào DB
    // =====================================================================

    private void saveVariants(String assetId, String hlsS3BasePath) {
        record Cfg(VariantType type, int w, int h, long bitrate, int order) {
        }

        List<Cfg> configs = List.of(
                new Cfg(VariantType.HLS_360P, 640, 360, 800_000L, 1),
                new Cfg(VariantType.HLS_480P, 854, 480, 1_400_000L, 2),
                new Cfg(VariantType.HLS_720P, 1280, 720, 2_800_000L, 3)
        );

        List<MediaAssetVariantEntity> variants = new ArrayList<>();
        for (Cfg c : configs) {
            String variantKey = hlsS3BasePath + "/"
                    + c.type().name().toLowerCase().replace("hls_", "") + ".m3u8";
            // HLS_360P → 360p.m3u8

            variants.add(MediaAssetVariantEntity.builder()
                    .assetId(assetId)
                    .variantType(c.type())
                    .bucket(bucket)
                    .objectKey(variantKey)
                    .mimeType("application/x-mpegURL")
                    .widthPx(c.w())
                    .heightPx(c.h())
                    .bitrate(c.bitrate())
                    .sortOrder(c.order())
                    .build());
        }

        variantRepository.saveAll(variants);
        log.info("Saved {} variant records for asset [{}]", variants.size(), assetId);
    }

    // =====================================================================
    // UTILS
    // =====================================================================

    private void deleteDirectory(Path dir) {
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path d, IOException e)
                        throws IOException {
                    Files.delete(d);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.warn("Failed to clean up work dir {}: {}", dir, e.getMessage());
        }
    }
}