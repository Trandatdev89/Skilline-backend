package com.project01.skillineserver.service.Impl;

import com.project01.skillineserver.dto.reponse.CourseResponse;
import com.project01.skillineserver.dto.reponse.PageResponse;
import com.project01.skillineserver.dto.request.CourseReq;
import com.project01.skillineserver.entity.CourseEntity;
import com.project01.skillineserver.entity.EnrollmentEntity;
import com.project01.skillineserver.enums.ErrorCode;
import com.project01.skillineserver.excepion.CustomException.AppException;
import com.project01.skillineserver.mapper.CourseMapper;
import com.project01.skillineserver.repository.CourseRepository;
import com.project01.skillineserver.repository.EnrollmentRepository;
import com.project01.skillineserver.service.CourseService;
import com.project01.skillineserver.specification.SearchCriteria;
import com.project01.skillineserver.specification.SearchSpecification;
import com.project01.skillineserver.utils.MapUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseMapper courseMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {AppException.class})
    public CourseEntity save(CourseReq courseReq) throws IOException {

        CourseEntity courseEntityInDB = Optional.ofNullable(courseReq.id())
                .flatMap(courseRepository::findById)
                .orElse(new CourseEntity());


        courseEntityInDB.setCategoryId(courseReq.categoryId());
        courseEntityInDB.setDescription(courseReq.description());
        courseEntityInDB.setDelete(true);
        courseEntityInDB.setPrice(courseReq.price());
        courseEntityInDB.setLevel(courseReq.level());
        courseEntityInDB.setDiscountPrice(courseReq.discount());
        courseEntityInDB.setTitle(courseReq.title());
        courseEntityInDB.setRate(courseReq.rate());
        courseEntityInDB.setPublishStatus(courseReq.publishStatus());
        courseEntityInDB.setAccessDurationUnit(courseReq.accessDurationUnit());
        courseEntityInDB.setAccessDurationValue(courseReq.accessDurationValue());
        courseEntityInDB.setThumbnailAssetId(courseReq.assetId());

        return courseRepository.save(courseEntityInDB);
    }

    @Override
    public void delete(List<String> courseId) {

        if (courseId == null || courseId.isEmpty()) {
            throw new AppException(ErrorCode.LIST_ID_EMPTY);
        }

        courseRepository.deleteAllByCourseIdIn(courseId);
    }

    @Override
    public CourseResponse getCourseById(Long id) {
        CourseEntity course = courseRepository.findByCourseId(id).orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        return courseMapper.toLectureResponse(course);
    }

    @Override
    public void purchaseCourse(List<Long> idCourse, Long userId) {
        List<Long> existingCourseIds = courseRepository.findAllIdsByIdIn(idCourse);

        if (existingCourseIds.size() != idCourse.size()) {

//            Set<Long> existingSet = new HashSet<>(existingCourseIds);
//            List<Long> notFoundIds = idCourse.stream()
//                    .filter(id -> !existingSet.contains(id))
//                    .collect(Collectors.toList());

            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }

        List<EnrollmentEntity> enrollmentEntities = idCourse.stream()
                .map(courseId -> EnrollmentEntity.builder()
                        .userId(userId)
                        .courseId(courseId)
                        .enrolledAt(Instant.now())
                        .progressPercent(0)
                        .build())
                .collect(Collectors.toList());

        enrollmentRepository.saveAll(enrollmentEntities);
    }

    @Override
    public PageResponse<CourseResponse> getCourses(int page, int size, String sort, String keyword, String categoryId) {
        Sort sortField = MapUtil.parseSort(sort);

        PageRequest pageRequest = PageRequest.of(page - 1, size, sortField);

        Page<CourseEntity> pageCourses = courseRepository.getCourses(keyword, categoryId, pageRequest);

        List<CourseResponse> courseResponseList = pageCourses.getContent().stream().map(courseMapper::toLectureResponse).toList();

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

        Pageable pageable = PageRequest.of(page - 1, size);

        Page<CourseEntity> listCourseResponses = null;
        Specification<CourseEntity> specification = Specification.where(null);

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

                specification = specification.and((root, query, criteriaBuilder) -> {
                    return new SearchSpecification<CourseEntity>(searchOpt).toPredicate(root, query, criteriaBuilder);
                });

//                if (searchOpt.getKey().equals("categoryId")) {
//                    specification = specification.and((root, query, criteriaBuilder) -> {
//                        return customCourseRepository.joinTableRelationOneMany(CourseEntity.class, CategoryEntity.class, root, criteriaBuilder, query, searchOpt);
//                    });
//                } else {
//                    specification = specification.and((root, query, criteriaBuilder) -> {
//                        return new SearchSpecification<CourseEntity>(searchOpt).toPredicate(root, query, criteriaBuilder);
//                    });
//                }
            }

            listCourseResponses = courseRepository.findAll(specification, pageable);

        } else {
            listCourseResponses = courseRepository.findAll(pageable);
        }

        return PageResponse.<CourseResponse>builder()
                .list(listCourseResponses.getContent().stream().map(courseMapper::toLectureResponse).toList())
                .size(size)
                .page(page)
                .totalPages(listCourseResponses.getTotalPages())
                .totalElements(listCourseResponses.getTotalElements())
                .build();

    }

    @Override
    public PageResponse<CourseResponse> getCoursesWithCursor(Instant cursor, String sort, String keyword, int size, String categoryId) {

        Page<CourseEntity> pages = courseRepository.getCoursesWithCursor(keyword, categoryId, cursor, size);

        List<CourseResponse> courseResponseList = pages.getContent().stream().map(courseMapper::toLectureResponse).toList();

        int indexLast = pages.getContent().size();
        Instant nextCursor = pages.getContent().get(indexLast - 1).getCreatedAt();

        return PageResponse.<CourseResponse>builder()
                .list(courseResponseList)
                .size(size)
                .nextCursor(nextCursor)
                .hasNextPage(size == indexLast)
                .totalElements(pages.getTotalElements())
                .totalPages(pages.getTotalPages())
                .build();
    }

    @Override
    public PageResponse<CourseResponse> getCoursesByMySelf(int page, int size, String sort, String keyword, String categoryId, Long userId) {
        Sort sortField = MapUtil.parseSort(sort);

        PageRequest pageRequest = PageRequest.of(page - 1, size, sortField);

        Page<CourseEntity> pageCourses = courseRepository.getCoursesByMySelf(keyword, categoryId, userId, pageRequest);

        List<CourseResponse> courseResponseList = pageCourses.getContent().stream().map(courseMapper::toLectureResponse).toList();

        return PageResponse.<CourseResponse>builder()
                .list(courseResponseList)
                .page(page)
                .size(size)
                .totalElements(pageCourses.getTotalElements())
                .totalPages(pageCourses.getTotalPages())
                .build();
    }

}
