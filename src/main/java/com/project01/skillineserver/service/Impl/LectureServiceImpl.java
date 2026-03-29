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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LectureServiceImpl implements LectureService {

    private final LectureRepository lectureRepository;
    private final MediaService mediaService;
    private final LectureMapper lectureMapper;

    @Override
    public LectureEntity save(LectureReq lectureReq) throws IOException, InterruptedException {

        boolean isUpdate = lectureReq.id()!=null;

        LectureEntity lectureEntity = isUpdate
                ? lectureRepository.findById(lectureReq.id())
                .orElseThrow(() -> new AppException(ErrorCode.LECTURE_NOT_FOUND))
                : new LectureEntity();

        Integer maxPositionLectureOfCourse = lectureRepository.findMaxPosition(lectureReq.courseId());

        lectureEntity.setTitle(lectureReq.title());
        lectureEntity.setPosition(maxPositionLectureOfCourse!=null ? maxPositionLectureOfCourse+1 : 0);
        lectureEntity.setCourseId(lectureReq.courseId());
        lectureEntity.setUpdatedAt(Instant.now());

        if(!isUpdate){
            lectureEntity.setCreatedAt(Instant.now());
        }

        LectureEntity lectureNeedSave = lectureRepository.save(lectureEntity);

        return lectureNeedSave;
    }

    @Override
    public PageResponse<LectureResponse> getListLecture(int page, int size, String sort, String keyword, Long courseId) {
        Sort sortField = MapUtil.parseSort(sort);
        PageRequest pageRequest = PageRequest.of(page - 1, size, sortField);

        Page<LectureEntity> orders = lectureRepository.getLectures(keyword,courseId,pageRequest);

        List<LectureResponse> listLectureResponse = orders.getContent().stream().map(lectureMapper::toLectureResponse).toList();

        return PageResponse.<LectureResponse>builder()
                .list(listLectureResponse)
                .page(page)
                .size(size)
                .totalElements(orders.getTotalElements())
                .totalPages(orders.getTotalPages())
                .build();
    }

    @Override
    public List<LectureResponse> getListLectureNotPagi(Long courseId) {
        List<LectureEntity> lectureEntityList = lectureRepository.findAllByCourseId(courseId);
        return lectureEntityList.stream().map(lectureMapper::toLectureResponse).toList();
    }

    @Override
    public Long countLectureInCourse(Long courseId) {
        return lectureRepository.countLectureByCourseId(courseId);
    }

}
