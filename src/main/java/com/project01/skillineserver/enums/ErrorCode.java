package com.project01.skillineserver.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;


@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ErrorCode {

    USER_NOT_FOUND(1001,"User not exits!", HttpStatus.NOT_FOUND),
    PASSWORD_WRONG(1002,"Password wrong!", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(1003,"Token not valid!", HttpStatus.UNAUTHORIZED),
    VERIFY_ACCOUNT(1005,"Account is not enable! Please confirm active account by email",HttpStatus.UNAUTHORIZED),
    ACCOUNT_IS_LOGOUT(1006,"Account is logout",HttpStatus.UNAUTHORIZED),
    USER_EXITED(1007,"Account is exited",HttpStatus.BAD_REQUEST),
    EMAIL_EXITED(1019,"Email is exited",HttpStatus.BAD_REQUEST),
    LIST_ID_EMPTY(1008,"List category id empty",HttpStatus.BAD_REQUEST),
    LECTURE_NOT_FOUND(1009,"Lecture not found",HttpStatus.NOT_FOUND),
    COURSE_EMPTY(1010,"Course is not empty",HttpStatus.BAD_REQUEST),
    COURSE_NOT_FOUND(1011,"Course is not found",HttpStatus.NOT_FOUND),
    UNAUTHORIZATED(1012,"Account is not authentication",HttpStatus.UNAUTHORIZED),
    FOBIDEN(1013,"Account is not permisson access resource this",HttpStatus.FORBIDDEN),
    ORDER_NOT_FOUND(1014,"Order not exits",HttpStatus.NOT_FOUND),
    VIDEO_CAN_NOT_UPLOAD(1015,"Video is can not uploaded",HttpStatus.INTERNAL_SERVER_ERROR),
    VIDEO_PROCESSING_FAILED(1016,"Video processing faile",HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_IS_LOCKED(1017,"Account is temporary locked in 5 minute",HttpStatus.LOCKED),
    PASSWORD_IS_EXPIRED(1018,"Password is expire .Please change password ensure account security",HttpStatus.BAD_REQUEST),
    ACCOUNT_LOGINED(1019,"Account is logined from divice other",HttpStatus.LOCKED),
    USER_SUBSCRIPTION_EMPTY(1020,"User subscription is empty",HttpStatus.NOT_FOUND),
    USER_NOT_BUY_COURSE(1021,"You not buy course that",HttpStatus.NOT_FOUND),
    FILE_REQUIRE(1022,"You need send file to body",HttpStatus.BAD_REQUEST),
    QUIZ_NOT_EXITS(1023,"This is Quiz not exits, maybe that is delete!",HttpStatus.NOT_FOUND),
    QUIZ_ID_REQUIRE(1024,"Please select quiz you need remove!",HttpStatus.BAD_REQUEST),
    QUESTION_NOT_EXITS(1025,"question is not exits",HttpStatus.NOT_FOUND),
    QUESTION_EMPTY(1026,"question is not exits",HttpStatus.BAD_REQUEST),
    QUIZ_MAX_FIVE(1027,"That is max 5 Quiz",HttpStatus.CONFLICT),
    REFRESH_TOKEN_EXPIRE(1028, "Session of you is exprire", HttpStatus.UNAUTHORIZED),
    MEDIA_ASSET_NOT_FOUND(1029, "Assest not found!", HttpStatus.NOT_FOUND),
    MEDIA_ASSET_ALREADY_PROCESSED(1030, "Assest not found!", HttpStatus.BAD_REQUEST),
    COURSE_NOT_PUBLISHER(1031, "Course dont public!", HttpStatus.BAD_REQUEST),
    COURSE_BOUGHT_ALREADY(1032, "You have bought course already!", HttpStatus.BAD_REQUEST),
    PAYMENT_FAILED(1033, "", HttpStatus.INTERNAL_SERVER_ERROR),
    MAIL_CONFIG_NOT_FOUND(1034, "You not yet config mail", HttpStatus.NOT_FOUND),

    INTERNAL_SERVER(9999,"Server error",HttpStatus.INTERNAL_SERVER_ERROR);

    private int code;
    private String message;
    private HttpStatus statusCode;

}
