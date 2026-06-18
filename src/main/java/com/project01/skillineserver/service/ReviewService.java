package com.project01.skillineserver.service;

import com.project01.skillineserver.dto.reponse.ReviewRes;
import com.project01.skillineserver.dto.request.ReviewReq;

import java.util.List;

public interface ReviewService {
    void createReview(ReviewReq reviewReq);
    List<ReviewRes> getReviewByCourseId(Long courseId);
}
