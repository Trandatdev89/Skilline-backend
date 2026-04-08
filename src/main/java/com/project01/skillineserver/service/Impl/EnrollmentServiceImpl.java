package com.project01.skillineserver.service.Impl;

import com.project01.skillineserver.config.CustomUserDetail;
import com.project01.skillineserver.entity.CourseEntity;
import com.project01.skillineserver.projection.CourseProjection;
import com.project01.skillineserver.repository.EnrollmentRepository;
import com.project01.skillineserver.service.EnrollmentService;
import com.project01.skillineserver.utils.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("enrollmentService")
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    @Override
    public List<CourseProjection> getListCourseUserBuy(Long userId) {
        return enrollmentRepository.getListCourseUserBuy(userId);
    }

    @Override
    public Boolean checkUserEnrollment(Long courseId) {
        CustomUserDetail customUserDetail = AuthenticationUtil.getUserDetail();
        assert customUserDetail != null;
        return enrollmentRepository.isUserEnrolledInCourse(customUserDetail.getUser().getId(),courseId) > 0;
    }
}
