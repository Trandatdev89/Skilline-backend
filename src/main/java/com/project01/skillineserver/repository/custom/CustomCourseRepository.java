package com.project01.skillineserver.repository.custom;

import com.project01.skillineserver.dto.reponse.CourseResponse;
import com.project01.skillineserver.dto.reponse.PageResponse;
import com.project01.skillineserver.specification.SearchCriteria;
import com.project01.skillineserver.specification.SearchOperation;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.Map;

public interface CustomCourseRepository {
    PageResponse<CourseResponse> searchAdvanceCourse(Map<String, Object> filters, int page, int size, String sort);

    <X,Y> Predicate joinTableRelationOneMany(Class<X> xClass, Class<Y> yClass, Root<X> root,
                                             CriteriaBuilder cb, CriteriaQuery<?> query, SearchCriteria searchCriteria);
}
