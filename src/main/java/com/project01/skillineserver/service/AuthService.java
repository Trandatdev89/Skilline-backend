package com.project01.skillineserver.service;

import com.project01.skillineserver.dto.reponse.AuthResponse;
import com.project01.skillineserver.dto.request.LoginRequest;
import com.project01.skillineserver.dto.request.RegisterRequest;
import com.project01.skillineserver.enums.TokenType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.text.ParseException;

public interface AuthService {
    AuthResponse login(LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response);
    boolean introspect(String token, TokenType tokenType);

    void refreshToken(String refreshToken, HttpServletResponse response) throws ParseException;
    void createAccount(RegisterRequest registerRequest) throws IllegalAccessException;
    void verifyAccount(String token,Long userId);

    void logout(String token, HttpServletResponse response) throws ParseException;
    void forgotPassword(String email);
}
