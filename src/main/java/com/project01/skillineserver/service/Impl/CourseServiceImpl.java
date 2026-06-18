package com.project01.skillineserver.service.Impl;

import com.project01.skillineserver.dto.reponse.CourseResponse;
import com.project01.skillineserver.dto.reponse.PageResponse;
import com.project01.skillineserver.dto.request.CourseReq;
import com.project01.skillineserver.entity.CourseEntity;
import com.project01.skillineserver.enums.ErrorCode;
import com.project01.skillineserver.excepion.CustomException.AppException;
import com.project01.skillineserver.mapper.CourseMapper;
import com.project01.skillineserver.repository.CourseRepository;
import com.project01.skillineserver.service.CourseService;
import com.project01.skillineserver.specification.SearchCriteria;
import com.project01.skillineserver.specification.SearchSpecification;
import com.project01.skillineserver.utils.ComputeUtil;
import com.project01.skillineserver.utils.MapUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final MapUtil mapUtil;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {AppException.class})
    public void save(CourseReq courseReq) throws IOException {

        boolean isUpdate = courseReq.id() != null;

        CourseEntity courseEntityInDB = isUpdate ? courseRepository.findById(courseReq.id())
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND))
                : new CourseEntity();

        courseEntityInDB.setCategoryId(courseReq.categoryId());
        courseEntityInDB.setDescription(courseReq.description());
        courseEntityInDB.setDelete(false);
        courseEntityInDB.setPriceOriginal(courseReq.price());
        courseEntityInDB.setLevel(courseReq.level());
        courseEntityInDB.setDiscount(courseReq.discount());
        courseEntityInDB.setTitle(courseReq.title());
        courseEntityInDB.setRate(courseReq.rate());
        courseEntityInDB.setPublishStatus(courseReq.publishStatus());
        courseEntityInDB.setAccessDurationUnit(courseReq.accessDurationUnit());
        courseEntityInDB.setAccessDurationValue(courseReq.accessDurationValue());
        courseEntityInDB.setPriceDiscount(ComputeUtil
                .computedPriceWhenDiscount(courseReq.price(), courseReq.discount()));
        if (courseReq.assetId() != null) {
            courseEntityInDB.setThumbnailAssetId(courseReq.assetId());
        } else {
            courseEntityInDB.setThumbnailAssetId(null);
        }

        courseRepository.save(courseEntityInDB);
    }

    @Override
    @Transactional
    public void delete(List<Long> courseId) {

        if (courseId == null || courseId.isEmpty()) {
            log.info("List course id is empty");
            throw new AppException(ErrorCode.LIST_ID_EMPTY);
        }

        courseRepository.deleteAllByCourseIdIn(courseId);
    }

    @Override
    public List<CourseResponse> getCourseByIds(List<Long> ids) {
        List<CourseEntity> course = courseRepository.findAllByCourseIdIn(ids);

        return mapUtil.handleComputedThumbnail(course
                , CourseEntity::getThumbnailAssetId
                , courseMapper::toCourseResponse);

    }

    @Override
    public PageResponse<CourseResponse> getCourses(int page, int size, String sort, String keyword, Long categoryId) {
        Sort sortField = MapUtil.parseSort(sort);

        PageRequest pageRequest = PageRequest.of(page - 1, size, sortField);

        Page<CourseEntity> pageCourses = courseRepository.getCourses(keyword, categoryId, pageRequest);

        List<CourseResponse> courseResponseList = mapUtil
                .handleComputedThumbnail(pageCourses.getContent()
                        , CourseEntity::getThumbnailAssetId
                        , courseMapper::toCourseResponse);

        return PageResponse.<CourseResponse>builder()
                .list(courseResponseList)
                .page(page)
                .size(size)
                .totalElements(pageCourses.getTotalElements())
                .totalPages(pageCourses.getTotalPages())
                .build();
    }

    @Override
    public PageResponse<CourseResponse> searchAdvanceCourse(String[] search, int page, int size, String sort) {

        Sort sortField = MapUtil.parseSort(sort);

        PageRequest pageRequest = PageRequest.of(page - 1, size, sortField);

        Specification<CourseEntity> specification = Specification
                .where((root, query, criteriaBuilder)
                        -> criteriaBuilder.equal(root.get("isDelete"), false));

        if (search != null && search.length > 0) {

            List<SearchCriteria> searchCriterias = new ArrayList<>();
            Pattern pattern = Pattern.compile("(\\w+?)([<:>~!])(.*)(\\p{Punct}?)(\\p{Punct}?)");

            for (String item : search) {
                Matcher matcher = pattern.matcher(item);
                if (matcher.find()) {
                    searchCriterias.add(new SearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5)));
                }
            }

            for (SearchCriteria searchOpt : searchCriterias) {
                specification = specification.and((root, query, criteriaBuilder) ->
                        new SearchSpecification<CourseEntity>(searchOpt).toPredicate(root, query, criteriaBuilder)
                );
            }
        }

        Page<CourseEntity> listCourseResponses = courseRepository.findAll(specification, pageRequest);

        List<CourseResponse> courseResponseList = mapUtil.handleComputedThumbnail(listCourseResponses.getContent()
                , CourseEntity::getThumbnailAssetId
                , courseMapper::toCourseResponse);

        return PageResponse.<CourseResponse>builder()
                .list(courseResponseList)
                .size(size)
                .page(page)
                .totalPages(listCourseResponses.getTotalPages())
                .totalElements(listCourseResponses.getTotalElements())
                .build();
    }

    @Override
    public PageResponse<CourseResponse> getCoursesWithCursor(Long cursor, String sort, String keyword, int size, Long categoryId) {

        log.info("cursor: {}, keyword: {}, size: {}, categoryId: {}", cursor, keyword, size, categoryId);

        Sort sortField = MapUtil.parseSort(sort);

        PageRequest pageRequest = PageRequest.of(0, size, sortField);

        Slice<CourseEntity> pages = courseRepository.getCoursesWithCursor(keyword, categoryId, cursor, pageRequest);

        List<CourseEntity> content = pages.getContent();

        List<CourseResponse> courseResponseList = mapUtil.handleComputedThumbnail(content
                , CourseEntity::getThumbnailAssetId
                , courseMapper::toCourseResponse);

        Long nextCursorId = pages.hasNext() ? content.getLast().getId() : null;

        return PageResponse.<CourseResponse>builder()
                .list(courseResponseList)
                .hasNextPage(pages.hasNext())
                .nextCursor(nextCursorId)
                .size(size)
                .build();
    }

    @Override
    public PageResponse<CourseResponse> getCoursesByMySelf(int page, int size, String sort, String keyword, Long categoryId, Long userId) {
        Sort sortField = MapUtil.parseSort(sort);

        PageRequest pageRequest = PageRequest.of(page - 1, size, sortField);

        Page<CourseEntity> pageCourses = courseRepository.getCoursesByMySelf(keyword, categoryId, userId, pageRequest);

        List<CourseResponse> courseResponseList = mapUtil.handleComputedThumbnail(pageCourses.getContent()
                , CourseEntity::getThumbnailAssetId
                , courseMapper::toCourseResponse);

        return PageResponse.<CourseResponse>builder()
                .list(courseResponseList)
                .page(page)
                .size(size)
                .totalElements(pageCourses.getTotalElements())
                .totalPages(pageCourses.getTotalPages())
                .build();
    }
}
