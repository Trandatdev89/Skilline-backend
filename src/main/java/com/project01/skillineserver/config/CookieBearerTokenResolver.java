package com.project01.skillineserver.config;

import com.project01.skillineserver.constants.AppConstants;
import com.project01.skillineserver.utils.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CookieBearerTokenResolver implements BearerTokenResolver {

    @Override
    public String resolve(HttpServletRequest request) {
        log.info("URI of browser request: {}", request.getRequestURI());
        if (request.getRequestURI().startsWith("/auth/")
                || request.getRequestURI().startsWith("/ws")) { // ← thêm dòng này
            return null;
        }

        return CookieUtil.getTokenFromCookie(AppConstants.ACCESS_TOKEN,request);
    }
}
