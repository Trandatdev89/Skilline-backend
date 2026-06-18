package com.project01.skillineserver.service;

import com.project01.skillineserver.config.CustomUserDetail;
import com.project01.skillineserver.dto.reponse.AuthResponse;
import com.project01.skillineserver.dto.request.ChangePasswordReq;
import com.project01.skillineserver.entity.UserEntity;

public interface UserService {
    UserEntity getMyInfo(Long id);
    void changePassword(ChangePasswordReq changePasswordReq,Long userId);
    void changeEmail(String newEmail,Long userId);

    AuthResponse me(CustomUserDetail customUserDetail);
}
