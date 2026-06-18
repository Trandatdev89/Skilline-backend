package com.project01.skillineserver.constants;

public class AppConstants {
    public static final int CHUNK_SIZE=1024*1024;//1MB
    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final long LOCK_TIME_DURATION = 5; // 15 phút
    public static final int CHANGE_PASSWORD_PERIODIC = 90;
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String REFRESH_TOKEN = "refreshToken";
}
