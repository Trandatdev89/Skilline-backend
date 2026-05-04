package com.project01.skillineserver.controller;

import com.project01.skillineserver.dto.ApiResponse;
import com.project01.skillineserver.dto.reponse.AuthResponse;
import com.project01.skillineserver.dto.request.LoginRequest;
import com.project01.skillineserver.dto.request.RegisterRequest;
import com.project01.skillineserver.enums.TokenType;
import com.project01.skillineserver.service.AuthService;
import com.project01.skillineserver.utils.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/auth")
@Slf4j
public class AuthController {

    @Value("${jwt.accessTokenName}")
    private String accessTokenName;

    @Value("${jwt.refreshTokenName}")
    private String refreshTokenName;

    private final AuthService authService;

    @PostMapping(value = "/login")
    public ApiResponse<AuthResponse> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        return ApiResponse.<AuthResponse>builder()
                .code(200)
                .message("Login Successful !")
                .data(authService.login(loginRequest,request,response))
                .build();
    }

    @GetMapping(value = "/introspect-token")
    public ApiResponse<Boolean> introspect(@RequestParam TokenType tokenType, HttpServletRequest request) {

        log.info("type token: {}", tokenType);
        String nameToken = tokenType == TokenType.ACCESS_TOKEN ? accessTokenName : refreshTokenName;
        String token = CookieUtil.getTokenFromCookie(nameToken ,request);
        return ApiResponse.<Boolean>builder()
                .code(200)
                .message("Token Valid!")
                .data(authService.introspect(token, tokenType))
                .build();
    }

    @PostMapping("/register")
    public ApiResponse<?> register(@RequestBody RegisterRequest registerDTO) throws IllegalAccessException {
        authService.createAccount(registerDTO);
        return ApiResponse.builder()
                .message("Hãy xác thực tài khoản bằng email")
                .code(200)
                .build();
    }

    @GetMapping(value = "/verify")
    public ApiResponse<?> verifyAccount(@RequestParam(name = "token") String token, @RequestParam(name = "userId") Long userId) {
        authService.verifyAccount(token, userId);
        return ApiResponse.builder()
                .message("Xác thực tài khoản thành công!")
                .code(200)
                .build();
    }

    @GetMapping(value = "/forgot-password")
    public ApiResponse<?> forgotPassword(@RequestParam(name = "email") String email) {
        authService.forgotPassword(email);
        return ApiResponse.builder()
                .message("Đổi mật khẩu thành công!")
                .code(200)
                .build();
    }

    @PostMapping(value = "/refresh-token")
    public ApiResponse<?> refreshToken(HttpServletRequest request, HttpServletResponse response) throws ParseException {
        String refreshToken = CookieUtil.getTokenFromCookie(refreshTokenName,request);
        String accessToken = CookieUtil.getTokenFromCookie(accessTokenName, request);
        authService.refreshToken(refreshToken, accessToken, response);
        return ApiResponse.<String>builder()
                .code(200)
                .message("Refresh Token Success!")
                .build();
    }

    @GetMapping(value = "/csrf-token")
    public Map<String,String> getCsrfToken(CsrfToken csrfToken){
        Map<String,String> response = new HashMap<>();
        response.put("csrfToken", csrfToken.getToken());
        response.put("headerName", csrfToken.getHeaderName());
        response.put("parameterName", csrfToken.getParameterName());
        return response;
    }

}
