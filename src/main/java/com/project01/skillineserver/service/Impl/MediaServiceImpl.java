package com.project01.skillineserver.service.Impl;

import com.project01.skillineserver.constants.AppConstants;
import com.project01.skillineserver.entity.LectureEntity;
import com.project01.skillineserver.enums.ErrorCode;
import com.project01.skillineserver.enums.ProcessStatus;
import com.project01.skillineserver.excepion.CustomException.AppException;
import com.project01.skillineserver.repository.LectureRepository;
import com.project01.skillineserver.service.MediaService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    @Value("${upload.directory.hls}")
    private String HLS_DIR;

    private final LectureRepository lectureRepository;

    @PostConstruct
    public void init() {
        File HLS_DIR_FOLDER = new File(HLS_DIR);
        if (!HLS_DIR_FOLDER.exists()) {
            HLS_DIR_FOLDER.mkdir();
            log.info("HLS_DIR FOLDER CREATED");
        }
    }

    @Async("videoProcessingExecutor")
    public void processVideoAsync(String videoId) {
        try {
            processVideo(videoId);
            CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            updateLectureStatus(videoId, ProcessStatus.FAILED);
            CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public ResponseEntity<Resource> streamBasicVideo(String id) {
        LectureEntity lectureEntity = lectureRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LECTURE_NOT_FOUND));

        String contentType = lectureEntity.getContentType();
        String filePath = lectureEntity.getFilePath();
        Path path = Paths.get(filePath);

        Resource resource = new FileSystemResource(path);

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);

    }

    @Override
    public ResponseEntity<Resource> streamRangeLecture(String id, String range) {
        System.out.println(range);
        LectureEntity video = lectureRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.LECTURE_NOT_FOUND));
        Path path = Paths.get(video.getFilePath());

        Resource resource = new FileSystemResource(path);

        String contentType = video.getContentType();

        if (contentType == null) {
            contentType = "application/octet-stream";

        }

        long fileLength = path.toFile().length();
        log.info("Length all file video :{}", fileLength);

        if (range == null) {
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);
        }

        long rangeStart;

        long rangeEnd;

        String[] ranges = range.replace("bytes=", "").split("-");
        rangeStart = Long.parseLong(ranges[0]);

        rangeEnd = rangeStart + AppConstants.CHUNK_SIZE - 1;

        if (rangeEnd >= fileLength) {
            rangeEnd = fileLength - 1;

            log.info("RangeEnd is max limit :{}", rangeEnd);
        }

        log.info("range start : {}", rangeStart);
        log.info("range end : {}", rangeEnd);
        InputStream inputStream;

        try {

            inputStream = Files.newInputStream(path);
            inputStream.skip(rangeStart);
            long contentLength = rangeEnd - rangeStart + 1;

            byte[] data = new byte[(int) contentLength];
            int read = inputStream.read(data, 0, data.length);
            log.info("read(number of bytes) : {}", read);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            headers.add("X-Content-Type-Options", "nosniff");
            headers.setContentLength(contentLength);

            return ResponseEntity
                    .status(HttpStatus.PARTIAL_CONTENT)
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new ByteArrayResource(data));


        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Resource> streamMasterPlaylist(String videoId) {
        try {
            Path playlistPath = Paths.get(HLS_DIR, videoId, "master.m3u8");

            if (!Files.exists(playlistPath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(playlistPath.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.apple.mpegurl"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public ResponseEntity<Resource> streamSegment(String videoId, String segmentName) {
        try {
            Path segmentPath = Paths.get(HLS_DIR, videoId, segmentName);

            if (!Files.exists(segmentPath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(segmentPath.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("video/mp2t"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000")
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public void processVideo(String videoId) throws IOException, InterruptedException {
        LectureEntity lectureEntityInDB = lectureRepository.findById(videoId)
                .orElseThrow(() -> new AppException(ErrorCode.LECTURE_NOT_FOUND));

        //update status Processing:
        lectureEntityInDB.setProcessStatus(ProcessStatus.PROCESSING);
        lectureRepository.save(lectureEntityInDB);

        String filePathOfLecture = lectureEntityInDB.getFilePath();
        Path pathLectureInDB = Paths.get(filePathOfLecture);
        Path streamVideoHls = Paths.get(HLS_DIR, videoId);
        Path outputPath = Files.createDirectories(streamVideoHls);

        String ffmpegCmd = String.format(
                "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s/segment_%%3d.ts\"  \"%s/master.m3u8\" ",
                pathLectureInDB, outputPath, outputPath
        );

        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", ffmpegCmd);

        processBuilder.inheritIO();

        Process process = processBuilder.start();
        int exit = process.waitFor();

        if (exit != 0) {
            lectureEntityInDB.setProcessStatus(ProcessStatus.FAILED);
            throw new AppException(ErrorCode.VIDEO_CAN_NOT_UPLOAD);
        }

        lectureEntityInDB.setProcessStatus(ProcessStatus.COMPLETED);
        lectureRepository.save(lectureEntityInDB);
    }

    private void updateLectureStatus(String videoId, ProcessStatus processStatus) {
        LectureEntity lecture = lectureRepository.findById(videoId)
                .orElseThrow(() -> new AppException(ErrorCode.LECTURE_NOT_FOUND));
        lecture.setProcessStatus(processStatus);
        lectureRepository.save(lecture);
    }
}
