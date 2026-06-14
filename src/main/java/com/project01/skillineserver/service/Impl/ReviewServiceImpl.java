package com.project01.skillineserver.service.Impl;

import com.project01.skillineserver.config.CustomUserDetail;
import com.project01.skillineserver.dto.reponse.ReviewRes;
import com.project01.skillineserver.dto.request.ReviewReq;
import com.project01.skillineserver.entity.ReviewEntity;
import com.project01.skillineserver.entity.UserEntity;
import com.project01.skillineserver.repository.ReviewRepository;
import com.project01.skillineserver.repository.UserRepository;
import com.project01.skillineserver.service.ReviewService;
import com.project01.skillineserver.utils.AuthenticationUtil;
import com.project01.skillineserver.utils.DateUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final DateUtil dateUtil;

    @Override
    @Transactional
    public void createReview(ReviewReq reviewReq) {
        log.info("Creating review: {}", reviewReq);
        CustomUserDetail customUserDetail = AuthenticationUtil.getUserDetail();
        reviewRepository.save(ReviewEntity.builder()
                .courseId(reviewReq.courseId())
                .userId(customUserDetail.getUser().getId())
                .comment(reviewReq.comment())
                .rating(reviewReq.rating())
                .build());
    }

    @Override
    public List<ReviewRes> getReviewByCourseId(Long courseId) {
        List<ReviewEntity> reviewsInDB = reviewRepository.findReviewEntitiesByCourseId(courseId);

        List<Long> userIds = reviewsInDB.stream().map(ReviewEntity::getUserId).toList();
        Map<Long, UserEntity> userMap = userRepository
                .findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(
                        UserEntity::getId,
                        userEntity -> userEntity
                ));
        return reviewsInDB.stream().map(reviewEntity -> {
            UserEntity user = userMap.get(reviewEntity.getUserId());
            return ReviewRes.builder()
                    .id(reviewEntity.getId())
                    .comment(reviewEntity.getComment())
                    .username(user != null ? user.getUsername() : null)
                    .rating(reviewEntity.getRating())
                    .avatar(user != null ? user.getAvatar() : null)
                    .courseId(reviewEntity.getCourseId())
                    .userId(reviewEntity.getUserId())
                    .createdAt(dateUtil.format(reviewEntity.getCreatedAt()))
                    .build();
        }).toList();
    }
}
