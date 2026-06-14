package com.project01.skillineserver.service.Impl;

import com.project01.skillineserver.dto.reponse.LectureResponse;
import com.project01.skillineserver.dto.reponse.PageResponse;
import com.project01.skillineserver.dto.request.LectureReq;
import com.project01.skillineserver.entity.LectureEntity;
import com.project01.skillineserver.enums.ErrorCode;
import com.project01.skillineserver.excepion.CustomException.AppException;
import com.project01.skillineserver.mapper.LectureMapper;
import com.project01.skillineserver.repository.LectureRepository;
import com.project01.skillineserver.service.LectureService;
import com.project01.skillineserver.service.MediaService;
import com.project01.skillineserver.utils.MapUtil;
import com.project01.skillineserver.utils.UploadUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LectureServiceImpl implements LectureService {

    private final LectureRepository lectureRepository;
    private final UploadUtil uploadUtil;
    private final MediaService mediaService;
    private final LectureMapper lectureMapper;

    @Override
    public void save(LectureReq lectureReq) throws IOException, InterruptedException {

        boolean isUpdate = lectureReq.id() != null;

        LectureEntity lectureEntity = isUpdate
                ? lectureRepository.findByIdAndCourseId(lectureReq.id(), lectureReq.courseId())
                .orElseThrow(() -> new AppException(ErrorCode.LECTURE_NOT_FOUND))
                : new LectureEntity();

        Integer maxPositionLectureOfCourse = lectureRepository.findMaxPosition(lectureReq.courseId());

        lectureEntity.setTitle(lectureReq.title());
        lectureEntity.setPosition(maxPositionLectureOfCourse != null ? maxPositionLectureOfCourse + 1 : 0);
        lectureEntity.setCourseId(lectureReq.courseId());
        lectureEntity.setDurationSeconds(lectureReq.durationSeconds());
        lectureEntity.setPublishStatus(lectureReq.publishStatus());
        lectureEntity.setPreviewable(lectureReq.previewable());

        //handle Video upload only if new file
        if(lectureReq.videoFile()!=null){
            Map<String,Object> videoInfo = resolveVideoPath(lectureReq.videoFile());
            if (videoInfo != null) {
                lectureEntity.setFilePath((String) videoInfo.get("filePath"));
                lectureEntity.setImage((String) videoInfo.get("image"));
            }
        }
        LectureEntity lectureNeedSave = lectureRepository.save(lectureEntity);

        if (lectureReq.videoFile() != null && !lectureReq.videoFile().isEmpty()) {
            mediaService.processVideoAsync(lectureNeedSave.getId());
        }
    }

    @Override
    public PageResponse<LectureResponse> getListLecture(int page, int size, String sort, String keyword, Long courseId) {
        Sort sortField = MapUtil.parseSort(sort);
        PageRequest pageRequest = PageRequest.of(page - 1, size, sortField);

        Page<LectureEntity> lecturePages = lectureRepository.getLectures(keyword, courseId, pageRequest);

        List<LectureResponse> listLectureResponse = lecturePages
                .stream()
                .map(lectureMapper::toLectureResponse)
                .toList();

        return PageResponse.<LectureResponse>builder()
                .list(listLectureResponse)
                .page(page)
                .size(size)
                .totalElements(lecturePages.getTotalElements())
                .totalPages(lecturePages.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public void delete(List<String> lectureIds) {

        if (lectureIds == null || lectureIds.isEmpty()) {
            log.info("List Lecture id is empty");
            throw new AppException(ErrorCode.LIST_ID_EMPTY);
        }

        lectureRepository.deleteAllByLectureIdIn(lectureIds);
    }

    private Map<String,Object> resolveVideoPath(MultipartFile inputFile){
        if(inputFile==null || inputFile.isEmpty()){
            return null;
        }
        try{
            return uploadUtil.generateVideoUrl(inputFile);
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new AppException(ErrorCode.VIDEO_PROCESSING_FAILED);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
