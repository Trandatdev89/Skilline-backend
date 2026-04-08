package com.project01.skillineserver.service.Impl;

import com.project01.skillineserver.config.CustomUserDetail;
import com.project01.skillineserver.entity.EnrollmentEntity;
import com.project01.skillineserver.projection.CourseProjection;
import com.project01.skillineserver.repository.EnrollmentRepository;
import com.project01.skillineserver.service.EnrollmentService;
import com.project01.skillineserver.utils.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("enrollmentService")
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    @Override
    public List<CourseProjection> getListCourseUserBought(Long userId) {
        return enrollmentRepository.getListCourseUserBought(userId);
    }

    @Override
    public Boolean checkUserEnrollment(List<Long> courseId) {
        CustomUserDetail customUserDetail = AuthenticationUtil.getUserDetail();
        assert customUserDetail != null;
        return enrollmentRepository.isUserEnrolledInCourse(customUserDetail.getUser().getId(), courseId) > 0;
    }

    @Override
    @Transactional
    public void enrollmentCourses(List<EnrollmentEntity> enrollmentsNeedSave) {
        enrollmentRepository.saveAll(enrollmentsNeedSave);
    }
}
