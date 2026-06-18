package com.project01.skillineserver.dto.request;

public record ReviewReq(Long reviewId,String comment,Integer rating,Long courseId) {
}
