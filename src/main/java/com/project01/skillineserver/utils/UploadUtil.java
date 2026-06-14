package com.project01.skillineserver.utils;

import com.project01.skillineserver.enums.ErrorCode;
import com.project01.skillineserver.enums.FileType;
import com.project01.skillineserver.excepion.CustomException.AppException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Component
public class UploadUtil {

    @Value("${upload.directory.video}")
    private String videoPath;

    @Value("${upload.directory.image}")
    private String imagePath;

    @Value("${upload.directory.pdf}")
    private String pdfPath;


    @PostConstruct
    public void init() {
        File fileVideo = new File(videoPath);
        File fileImage = new File(imagePath);
        if(!fileVideo.exists()) {
            fileVideo.mkdirs();
        }
        if(!fileImage.exists()) {
            fileImage.mkdirs();
        }
    }

    public Map<String,Object> generateVideoUrl(MultipartFile lectureFile) throws IOException, InterruptedException {
        Map<String,Object> claimVideo = new HashMap<>();
        String pathVideo = createPathFile(lectureFile, FileType.VIDEO);

        String durationVideo = getVideoDuration(pathVideo);
        String imageVideo = extractThumbnail(pathVideo);

        claimVideo.put("filePath",pathVideo);
        claimVideo.put("contentType",lectureFile.getContentType());
        claimVideo.put("duration",durationVideo);
        claimVideo.put("image",imageVideo);

        return claimVideo;
    }

    public String convertVideoUrl(String fileName,String folder){
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder
                .getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        return request.getScheme() + "://" + request.getServerName()+":"
                + request.getServerPort()+ "/" + folder+ "/" + fileName;
    }

    private String getVideoDuration(String filePath) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "ffprobe",
                "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                filePath
        );

        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String duration = reader.readLine();
        process.waitFor();
        return duration;
    }

    public String extractThumbnail(String videoPath) throws IOException, InterruptedException {

        String fileName = UUID.randomUUID().toString() + ".jpg";

        Path folderImage = Paths.get(imagePath);

        Path imageFilePath = folderImage.resolve(fileName).normalize().toAbsolutePath();


        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-i", videoPath,
                "-ss", "00:00:05",
                "-vframes", "1",
                imageFilePath.toString()
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Failed to extract thumbnail from video: " + videoPath);
        }

        return fileName;

    }

    public String createPathFile(MultipartFile lectureFile, FileType fileType) throws IOException {


        String originFileName = lectureFile.getOriginalFilename();
        Path folderUpload;

        switch (fileType) {
            case VIDEO -> folderUpload = Paths.get(videoPath);
            case PDF -> folderUpload = Paths.get(pdfPath);
            default -> folderUpload = Paths.get(imagePath);
        }

        String fileExtension = StringUtils.getFilenameExtension(originFileName);

        List<String> allowedExtensions = switch (fileType) {
            case IMAGE -> List.of("jpg", "jpeg", "png", "webp");
            case VIDEO -> List.of("mp4", "mkv", "avi", "mov", "webm");
            case PDF -> List.of("pdf");
            case EXCEL -> null;
        };
        if (fileExtension != null && !allowedExtensions.contains(fileExtension.toLowerCase())) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }

        String fileName = Objects.isNull(fileExtension)
                ? UUID.randomUUID().toString()
                : UUID.randomUUID().toString() + "." + fileExtension;

        Path filePath = folderUpload.resolve(fileName).normalize().toAbsolutePath();

        if (!filePath.startsWith(folderUpload.toAbsolutePath())) {
            throw new SecurityException("Invalid file path detected");
        }

        Files.copy(lectureFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return folderUpload.toString().replace("\\", "/") + "/" + fileName;
    }

}
