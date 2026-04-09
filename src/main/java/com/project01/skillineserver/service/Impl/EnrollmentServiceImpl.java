package com.project01.skillineserver.service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project01.skillineserver.entity.CourseEntity;
import com.project01.skillineserver.entity.EnrollmentEntity;
import com.project01.skillineserver.enums.ErrorCode;
import com.project01.skillineserver.enums.PublishStatus;
import com.project01.skillineserver.excepion.CustomException.AppException;
import com.project01.skillineserver.kafka.event.EnrollmentEvent;
import com.project01.skillineserver.projection.CourseProjection;
import com.project01.skillineserver.repository.CourseRepository;
import com.project01.skillineserver.repository.EnrollmentRepository;
import com.project01.skillineserver.service.EnrollmentService;
import com.project01.skillineserver.service.PushNotificationService;
import com.project01.skillineserver.utils.CalculatorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service("enrollmentService")
@RequiredArgsConstructor
@Slf4j
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final ObjectMapper objectMapper;
    private final PushNotificationService pushNotificationService;

    @Override
    public List<CourseProjection> getListCourseUserBought(Long userId) {
        return enrollmentRepository.getListCourseUserBought(userId);
    }

    @Override
    public Boolean checkUserEnrollment(List<Long> courseId, Long userId) {
        return enrollmentRepository.isUserEnrolledInCourse(userId, courseId) > 0;
    }

    @Transactional
    @KafkaListener(topics = "${app.kafka.enrollment-course:enrollment-course}"
            , groupId = "media-processing-group"
            , containerFactory = "kafkaListenerContainerFactory")
    public void enrollmentCourses(ConsumerRecord<String, Object> record, Acknowledgment acknowledgment) {

        log.info("[Kafka] Received enrollment-course | orderId={}", record.key());

        EnrollmentEvent event;
        try {
            event = objectMapper.convertValue(record.value(), EnrollmentEvent.class);
        } catch (Exception e) {
            log.error("Cannot deserialize event key={}: {}", record.key(), e.getMessage());
            acknowledgment.acknowledge();
            return;
        }

        List<CourseEntity> listCourseInDB = courseRepository
                .findAllByCourseStatusPublishIdIn(event.getCoursesId(), PublishStatus.PUBLISHER)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        List<EnrollmentEntity> enrollmentEntities = listCourseInDB.stream()
                .map(item -> EnrollmentEntity.builder()
                        .userId(event.getUserId())
                        .courseId(item.getId())
                        .timeExpire(CalculatorUtil.computedTimeExpireEnrollment(item.getAccessDurationValue(), item.getAccessDurationUnit()))
                        .enrolledAt(Instant.now())
                        .progressPercent(0)
                        .build())
                .collect(Collectors.toList());
        enrollmentRepository.saveAll(enrollmentEntities);

        log.info("Subscribe enrollment course success! with coursesId : {} and userId : {}", event.getCoursesId().toString(), event.getUserId());

        //send notification
        pushNotificationService.sendNotification(event.getUserId(), "Dang ky khoa hoc thanh cong!", "Chuc mung ban da dang ky thanh cong khoa hoc.");

        //send mail


    }
}
