package com.project01.skillineserver.service.Impl;

import com.project01.skillineserver.config.CustomUserDetail;
import com.project01.skillineserver.dto.reponse.AuthResponse;
import com.project01.skillineserver.dto.request.ChangePasswordReq;
import com.project01.skillineserver.entity.UserEntity;
import com.project01.skillineserver.enums.ErrorCode;
import com.project01.skillineserver.excepion.CustomException.AppException;
import com.project01.skillineserver.repository.UserRepository;
import com.project01.skillineserver.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserEntity getMyInfo(Long id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public void changePassword(ChangePasswordReq changePasswordReq, Long userId) {
        UserEntity user = userRepository
                .findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(changePasswordReq.oldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_WRONG);
        }

        user.setLastTimeChangePassword(Instant.now());
        user.setPassword(passwordEncoder.encode(changePasswordReq.newPassword()));
        userRepository.save(user);
    }

    @Override
    public void changeEmail(String newEmail, Long userId) {
        UserEntity user = userRepository
                .findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setEmail(newEmail);
        userRepository.save(user);
    }

    @Override
    public AuthResponse me(CustomUserDetail customUserDetail) {

        UserEntity userEntityInUserDetail = customUserDetail.getUser();

        return AuthResponse.builder()
                .role(userEntityInUserDetail.getRole())
                .userId(userEntityInUserDetail.getId())
                .authenticated(true)
                .avatar(userEntityInUserDetail.getAvatar())
                .username(userEntityInUserDetail.getUsername())
                .email(userEntityInUserDetail.getEmail())
                .phone(userEntityInUserDetail.getPhone())
                .build();
    }

}
