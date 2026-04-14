package com.project01.skillineserver.controller;

import com.project01.skillineserver.config.CustomUserDetail;
import com.project01.skillineserver.constants.AppConstants;
import com.project01.skillineserver.dto.ApiResponse;
import com.project01.skillineserver.dto.reponse.AuthResponse;
import com.project01.skillineserver.dto.request.ChangeEmailReq;
import com.project01.skillineserver.dto.request.ChangePasswordReq;
import com.project01.skillineserver.entity.UserEntity;
import com.project01.skillineserver.service.AuthService;
import com.project01.skillineserver.service.UserService;
import com.project01.skillineserver.utils.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final AuthService authService;

    @GetMapping(value = "/info")
    @PreAuthorize("@authorizationService.isCanAccessApi()")
    public ApiResponse<UserEntity> getMyInfo(@AuthenticationPrincipal CustomUserDetail customUserDetail) {

        Long userId = customUserDetail.getUser().getId();

        return ApiResponse.<UserEntity>builder()
                .message("get info success!")
                .code(200)
                .data(userService.getMyInfo(userId))
                .build();
    }

    @GetMapping(value = "/me")
    @PreAuthorize("@authorizationService.isCanAccessApi()")
    public ApiResponse<AuthResponse> me(HttpServletRequest request) {
        return ApiResponse.<AuthResponse>builder()
                .code(200)
                .message("Token Valid!")
                .data(userService.me())
                .build();
    }

    @GetMapping(value = "/logout")
    @PreAuthorize("@authorizationService.isCanAccessApi()")
    public ApiResponse<?> logout(HttpServletRequest request, HttpServletResponse response) throws ParseException {
        String token = CookieUtil.getTokenFromCookie(AppConstants.ACCESS_TOKEN,request);
        authService.logout(token, response);
        return ApiResponse.builder()
                .message("Logout Success!")
                .code(200)
                .build();
    }

    @PostMapping(value = "/change-password")
    @PreAuthorize("@authorizationService.isCanAccessApi()")
    public ApiResponse<?> changePassword(@RequestBody ChangePasswordReq changePasswordReq
            , @AuthenticationPrincipal CustomUserDetail customUserDetail) {
        userService.changePassword(changePasswordReq,customUserDetail.getUser().getId());
        return ApiResponse.builder()
                .message("Change Password Success!")
                .code(200)
                .build();
    }

    @PostMapping(value = "/change-email")
    @PreAuthorize("@authorizationService.isCanAccessApi()")
    public ApiResponse<?> changeEmail(@RequestBody ChangeEmailReq changeEmailReq, @AuthenticationPrincipal CustomUserDetail customUserDetail, HttpServletRequest request) {
        System.out.println("🚨 CSRF ATTACK via FORM: Email changed to " + changeEmailReq.newEmail());

        String csrfTokenGetHeader = request.getHeader("X-XSRF-TOKEN");
        log.info("Token csrf from header of browser: {}", csrfTokenGetHeader);

        CsrfToken csrfTokenInRepo = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        String expected = csrfTokenInRepo != null ? csrfTokenInRepo.getToken() : null;

        log.info("Token csrf from repo of browser: {}", expected);

        userService.changeEmail(changeEmailReq.newEmail(),customUserDetail.getUser().getId());
        return ApiResponse.builder()
                .message("Change Mail Success!")
                .code(200)
                .build();
    }

}
