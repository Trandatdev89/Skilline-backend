package com.project01.skillineserver.config;

import com.project01.skillineserver.enums.ErrorCode;
import com.project01.skillineserver.excepion.CustomException.AppException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
public class CsrfValidationFilter extends OncePerRequestFilter {

    private final CsrfTokenRepository csrfTokenRepository;

    private static final String[] IGNORED_PATHS = {
            "/auth/", "/api/file/", "/ws/", "/api/test", "/api/media/"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String method = request.getMethod();
        String path = request.getRequestURI();

        // Skip nếu là path được ignore
        if (isIgnoredPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!"GET".equals(method) && !"HEAD".equals(method) &&
                !"TRACE".equals(method) && !"OPTIONS".equals(method)) {

            CsrfToken csrfToken = csrfTokenRepository.loadToken(request);

            if (csrfToken == null) {
                throw new MissingCsrfTokenException("CSRF token not found in session");
            }

            String actualToken = request.getHeader("X-XSRF-TOKEN");

            if (actualToken == null) {
                actualToken = request.getHeader("X-CSRF-TOKEN");
            }

            if (actualToken == null) {
                throw new AppException(ErrorCode.FOBIDEN);
            }

            if (!csrfToken.getToken().equals(actualToken)) {
                throw new InvalidCsrfTokenException(csrfToken, actualToken);
            }

            System.out.println("✅ CSRF validation PASSED!");
        }

        filterChain.doFilter(request, response);
    }

    private boolean isIgnoredPath(String path) {
        for (String ignored : IGNORED_PATHS) {
            if (path.startsWith(ignored)) return true;
        }
        return false;
    }
}
