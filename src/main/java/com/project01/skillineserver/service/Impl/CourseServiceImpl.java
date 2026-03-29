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
import com.project01.skillineserver.repository.custom.CustomCourseRepository;
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
import java.time.LocalDateTime;
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
    private final CustomCourseRepository customCourseRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {AppException.class})
    public CourseEntity save(CourseReq courseReq) throws IOException {

        CourseEntity courseEntityInDB = Optional.ofNullable(courseReq.id())
                .flatMap(courseRepository::findById)
                .orElse(new CourseEntity());


        courseEntityInDB.setCategoryId(courseReq.categoryId());
        courseEntityInDB.setDescription(courseReq.desc());
        courseEntityInDB.setDelete(courseReq.isDeleted() != null ? courseReq.isDeleted() : false);
        courseEntityInDB.setPrice(courseReq.price());
        courseEntityInDB.setLevel(courseReq.level());
        courseEntityInDB.setDiscountPrice(courseReq.discount());
        courseEntityInDB.setTitle(courseReq.title());
        courseEntityInDB.setRate(courseReq.rate());

        return courseRepository.save(courseEntityInDB);
    }

    @Override
    public void delete(List<Long> courseId) {
        List<CourseEntity> courseEntityListInDB = courseRepository.findAllByCourseIdIn(courseId);
        courseEntityListInDB.forEach(courseEntity -> {
            courseEntity.setDelete(false);
        });
        courseRepository.saveAll(courseEntityListInDB);
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
    public List<CourseResponse> getListCourseById(List<Long> ids) {
        return courseRepository.findAllByCourseIdIn(ids).stream().map(courseMapper::toLectureResponse).toList();
    }

    @Override
    public PageResponse<CourseResponse> getCourses(int page, int size, String sort, String keyword,Long categoryId) {
        Sort sortField = MapUtil.parseSort(sort);

        PageRequest pageRequest = PageRequest.of(page - 1, size, sortField);

        Page<CourseEntity> pageCourses = courseRepository.getCourses(keyword,categoryId,pageRequest);

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
    public PageResponse<CourseResponse> getCoursesWithCursor(LocalDateTime cursor, String sort, String keyword, int size) {

//        Map<String,String> sortField = JavaUtil.extractFieldToMap(sort);
//        Map<String,String> keywordField = JavaUtil.extractFieldToMap(keyword);
//
//        Page<CourseEntity> pages = courseRepository.findAllByCursor(cursor,sortField.get,keywordField,size);
        return null;
    }

    @Override
    public PageResponse<CourseResponse> getCoursesByMySelf(int page, int size, String sort, String keyword, Long categoryId, Long userId) {
        Sort sortField = MapUtil.parseSort(sort);

        PageRequest pageRequest = PageRequest.of(page - 1, size, sortField);

        Page<CourseEntity> pageCourses = courseRepository.getCoursesByMySelf(keyword,categoryId,userId,pageRequest);

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
