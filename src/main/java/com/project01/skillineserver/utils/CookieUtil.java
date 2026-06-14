package com.project01.skillineserver.utils;


import com.project01.skillineserver.constants.AppConstants;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CookieUtil {

    public static void setAccessTokenCookieHttpOnly(String accessToken, HttpServletResponse response){
        Cookie accessTokenCookie = new Cookie(AppConstants.ACCESS_TOKEN, accessToken);
//        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(false);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(30 * 24 * 60 * 60); //1 ngay
        accessTokenCookie.setAttribute("SameSite", "Strict");
        response.addCookie(accessTokenCookie);
    }

    public static void setRefreshTokenCookieHttpOnly(String refreshTokenCookieHttpOnly, HttpServletResponse response){
        Cookie refreshTokenCookie = new Cookie(AppConstants.REFRESH_TOKEN, refreshTokenCookieHttpOnly);
//        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(30 * 24 * 60 * 60);
        refreshTokenCookie.setAttribute("SameSite", "Strict");
        response.addCookie(refreshTokenCookie);
    }

    public static void setDeviceIdCookieHttpOnly(String currentDeviceId, HttpServletResponse response){
        Cookie deviceIdCookie = new Cookie("deviceId", currentDeviceId);
        deviceIdCookie.setHttpOnly(false); // Frontend cần đọc được
        deviceIdCookie.setSecure(false);
        deviceIdCookie.setPath("/");
        deviceIdCookie.setMaxAge(30 * 24 * 60 * 60);
        deviceIdCookie.setAttribute("SameSite", "Strict");
        response.addCookie(deviceIdCookie);
    }

    public static String getTokenFromCookie(String name, HttpServletRequest request){
        if(request.getCookies()==null){
            return null;
        }
        for (Cookie c : request.getCookies()){
            if(name.equals(c.getName())){
                return c.getValue();
            }
        }
        return null;
    }

    public static void deleteCookie(String name, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
        log.info("Deleted cookie: {}", name);
    }

    public static void deleteAccessTokenCookie(HttpServletResponse response) {
        deleteCookie(AppConstants.ACCESS_TOKEN, response);
    }

    public static void deleteRefreshTokenCookie(HttpServletResponse response) {
        deleteCookie(AppConstants.REFRESH_TOKEN, response);
    }

    public static void deleteDeviceIdCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("deviceId", null);
        cookie.setHttpOnly(false); // Phải match với khi tạo
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
        log.info("Deleted cookie: deviceId");
    }

    public static void clearAllAuthCookies(HttpServletResponse response) {
        deleteAccessTokenCookie(response);
        deleteRefreshTokenCookie(response);
        deleteDeviceIdCookie(response);
        log.info("Cleared all authentication cookies");
    }
}
