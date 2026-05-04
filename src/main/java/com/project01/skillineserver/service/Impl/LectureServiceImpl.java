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
import com.project01.skillineserver.utils.MapUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LectureServiceImpl implements LectureService {

    private final LectureRepository lectureRepository;
    private final LectureMapper lectureMapper;
    private final MapUtil mapUtil;

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
        lectureEntity.setContentAssetId(lectureReq.contentAssetId());
        lectureEntity.setThumbnailAssetId(lectureReq.thumbnailAssetId());
        lectureEntity.setDurationSeconds(lectureReq.durationSeconds());
        lectureEntity.setPublishStatus(lectureReq.publishStatus());
        lectureEntity.setPreviewable(lectureReq.previewable());

        lectureRepository.save(lectureEntity);

    }

    @Override
    public PageResponse<LectureResponse> getListLecture(int page, int size, String sort, String keyword, Long courseId) {
        Sort sortField = MapUtil.parseSort(sort);
        PageRequest pageRequest = PageRequest.of(page - 1, size, sortField);

        Page<LectureEntity> lecturePages = lectureRepository.getLectures(keyword, courseId, pageRequest);

        List<LectureResponse> listLectureResponse = mapUtil.handleComputedThumbnail(lecturePages.getContent()
                , LectureEntity::getThumbnailAssetId
                , lectureMapper::toLectureResponse);

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
}
