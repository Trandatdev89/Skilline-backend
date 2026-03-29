package com.project01.skillineserver.service.Impl;

import com.project01.skillineserver.config.CustomUserDetail;
import com.project01.skillineserver.enums.ErrorCode;
import com.project01.skillineserver.enums.Role;
import com.project01.skillineserver.excepion.CustomException.AppException;
import com.project01.skillineserver.utils.AuthenticationUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;


@Service(value = "authorizationService")
public class AuthorizationService {

    public boolean isAdmin() throws AccessDeniedException {


        CustomUserDetail customUserDetail = AuthenticationUtil.getUserDetail();
        if (customUserDetail == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        Role role = customUserDetail.getUser().getRole();
        if (role != Role.ADMIN) {
            throw new AppException(ErrorCode.FOBIDEN);
        }

        return true;
    }

    public boolean isUserNormal() {

        CustomUserDetail customUserDetail = AuthenticationUtil.getUserDetail();
        assert customUserDetail != null;
        Role role = customUserDetail.getUser().getRole();

        if (role != Role.USER) {
            throw new AppException(ErrorCode.FOBIDEN);
        }

        return true;
    }

    public boolean isCanAccessApi() {
        CustomUserDetail customUserDetail = AuthenticationUtil.getUserDetail();
        return customUserDetail != null;
    }

    private boolean checkAuthen() {
        Authentication authentication = AuthenticationUtil.getAuthentication();
        if (!AuthenticationUtil.isAuthenticated(authentication)) {
            return true;
        }
        return false;
    }
}
