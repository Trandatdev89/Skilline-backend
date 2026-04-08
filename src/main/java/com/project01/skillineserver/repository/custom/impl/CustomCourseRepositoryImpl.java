package com.project01.skillineserver.repository.custom.impl;

import com.project01.skillineserver.dto.reponse.CourseResponse;
import com.project01.skillineserver.dto.reponse.PageResponse;
import com.project01.skillineserver.entity.CourseEntity;
import com.project01.skillineserver.mapper.CourseMapper;
import com.project01.skillineserver.repository.custom.CustomCourseRepository;
import com.project01.skillineserver.specification.SearchCriteria;
import com.project01.skillineserver.specification.SearchSpecification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
@RequiredArgsConstructor
public class CustomCourseRepositoryImpl implements CustomCourseRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    private final CourseMapper courseMapper;

    @Override
    public PageResponse<CourseResponse> searchAdvanceCourse(Map<String, Object> filters, int page, int size, String sort) {
        StringBuilder sb = new StringBuilder("select co.id,co.title,co.price,co.level" +
                ",co.thumbnail_url,co.rate,co.created_at,co.updated_at,co.category_id from courses co");

        Map<String, Object> dataConvert = convertMap(filters);

        if (dataConvert.containsKey("category_id")) {
            joinTableCourseWithCategory(dataConvert, sb);
        }

        sb.append(" where 1=1");

        queryNormal(dataConvert, sb);

        Query countQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM (" + sb.toString() + ") as total"
        );
        long totalElements = ((Number) countQuery.getSingleResult()).longValue();

        sb.append(" limit " + size + " offset " + page * size);

        Query query = entityManager.createNativeQuery(sb.toString());

        List<CourseEntity> courses = query.getResultList();
        List<CourseResponse> courseResponses = courses.stream().map(courseMapper::toLectureResponse).toList();

        return PageResponse.<CourseResponse>builder()
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages((int) Math.ceil((double) totalElements / size))
                .list(courseResponses)
                .build();
    }

    @Override
    public <X, Y> Predicate joinTableRelationOneMany(Class<X> xClass, Class<Y> yClass, Root<X> root,
                                                     CriteriaBuilder cb, CriteriaQuery<?> query, SearchCriteria searchCriteria) {
        Join<X, Y> joinTable = root.join(yClass.getName().toLowerCase(), JoinType.INNER);

        return new SearchSpecification<X>(searchCriteria).toPredicate((Root<X>) joinTable, query, cb);
    }

    private void joinTableCourseWithCategory(Map<String, Object> filters, StringBuilder sb) {
        Object categoryId = filters.get("category_id");
        if (categoryId != null) {
            sb.append(" inner join category ca on ca.id=co.category_id ");
        }
    }

    private void queryNormal(Map<String, Object> filters, StringBuilder sb) {
        for (Map.Entry<String, Object> field : filters.entrySet()) {
            String columnName = field.getKey();
            Object value = field.getValue();
            String valueStr = value.toString();
            if (columnName.startsWith("price") || columnName.startsWith("rate")) {
                querySpecial(columnName, value, sb);
            } else if (isNumeric(valueStr)) {
                sb.append(" and co.").append(columnName).append("=").append(valueStr);
            } else {
                sb.append(" and co.").append(columnName).append(" like '%").append(valueStr).append("%'");
            }
        }
    }

    private void querySpecial(String columName, Object value, StringBuilder sb) {

        if (columName.startsWith("price")) {
            if (columName.equals("priceStart")) {
                sb.append(" and co.price >= " + value);
            }

            if (columName.equals("priceEnd")) {
                sb.append(" and co.price <= " + value);
            }
        }

        if (columName.startsWith("rate")) {
            if (columName.equals("rateStart")) {
                sb.append(" and co.rate >= " + value);
            }

            if (columName.equals("rateEnd")) {
                sb.append(" and co.rate <= " + value);
            }
        }


    }

    private Map<String, Object> convertMap(Map<String, Object> filters) {
        Map<String, Object> convertData = new HashMap<>();
        for (Map.Entry<String, Object> field : filters.entrySet()) {
            String columName = field.getKey();
            Object value = field.getValue();
            if (value != null && !value.toString().trim().isEmpty()) {
                convertData.put(columName, value);
            }
        }
        return convertData;
    }

    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
