package com.project01.skillineserver.config;

import com.project01.skillineserver.constants.AppConstants;
import com.project01.skillineserver.utils.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();

            // Lấy token từ cookie NGAY LÚC NÀY, khi request còn sống
            String token = CookieUtil.getTokenFromCookie(
                    AppConstants.ACCESS_TOKEN,
                    httpRequest
            );

            if (token != null) {
                attributes.put("ACCESS_TOKEN", token); // ← lưu token, không lưu request
            }
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
