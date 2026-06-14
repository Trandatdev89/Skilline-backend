package com.project01.skillineserver.excepion;


import com.project01.skillineserver.dto.ApiResponse;
import com.project01.skillineserver.excepion.CustomException.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GolbalException {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<?>> appException(AppException ex) {
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .message(ex.getErrorCode().getMessage())
                .code(ex.getErrorCode().getCode())
                .build();
        return ResponseEntity.status(ex.getErrorCode().getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(MissingCsrfTokenException.class)
    public ResponseEntity<ApiResponse<?>> missingCsrfTokenException(MissingCsrfTokenException ex) {
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .message(ex.getMessage())
                .code(403)
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResponse);
    }

    @ExceptionHandler(InvalidCsrfTokenException.class)
    public ResponseEntity<ApiResponse<?>> invalidCsrfTokenException(InvalidCsrfTokenException ex) {
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .message(ex.getMessage())
                .code(403)
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResponse);
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiResponse<?>> handleAll(Exception ex) {
//        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER;
//        ApiResponse<?> apiResponse = ApiResponse.builder()
//                .message(ex.getMessage())
//                .code(errorCode.getCode())
//                .build();
//        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
//    }


}