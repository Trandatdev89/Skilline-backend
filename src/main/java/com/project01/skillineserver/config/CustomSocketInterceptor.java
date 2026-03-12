package com.project01.skillineserver.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import com.project01.skillineserver.enums.ErrorCode;
import com.project01.skillineserver.enums.TokenType;
import com.project01.skillineserver.excepion.CustomException.AppException;
import com.project01.skillineserver.socket.StompPrincipal;
import com.project01.skillineserver.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.text.ParseException;

@RequiredArgsConstructor
@Component
@Slf4j
public class CustomSocketInterceptor implements ChannelInterceptor {

    private final SecurityUtil securityUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            StompCommand command = accessor.getCommand();

            if (StompCommand.CONNECT.equals(command)) {
                try {
                    handleConnect(accessor);
                } catch (ParseException e) {
                    log.error("ParseException: {}", e.getMessage());
                    throw new RuntimeException(e);
                } catch (JOSEException e) {
                    log.error("JOSEException: {}", e.getMessage());
                    throw new RuntimeException(e);
                } catch (Exception e) {
                    log.error("Unknown exception: {}", e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) throws ParseException, JOSEException {
        String accessToken = extractToken(accessor);

        if (accessToken == null || accessToken.isEmpty()) {
            throw new AppException(ErrorCode.UNAUTHORIZATED);
        }

        SignedJWT signedJWT = securityUtil.verifyToken(accessToken, TokenType.ACCESS_TOKEN);

        String userId = signedJWT.getJWTClaimsSet().getSubject();

        StompPrincipal stompPrincipal = new StompPrincipal(userId);

        accessor.setUser(stompPrincipal);

        accessor.getSessionAttributes().put("userId",userId);

        accessor.setHeader("authenticated", true);

        accessor.setHeader("username", userId);
    }

    private String extractToken(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        Object token = accessor.getSessionAttributes().get("ACCESS_TOKEN");
        if (token != null) {
            return token.toString();
        }

        return null;
    }
}
