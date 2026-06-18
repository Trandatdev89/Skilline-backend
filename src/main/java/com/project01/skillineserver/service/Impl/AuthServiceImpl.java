package com.project01.skillineserver.service.Impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import com.project01.skillineserver.config.CustomUserDetail;
import com.project01.skillineserver.constants.AppConstants;
import com.project01.skillineserver.dto.reponse.AuthResponse;
import com.project01.skillineserver.dto.request.LoginRequest;
import com.project01.skillineserver.dto.request.RegisterRequest;
import com.project01.skillineserver.dto.request.VerifyAccountRequest;
import com.project01.skillineserver.entity.UserDevice;
import com.project01.skillineserver.entity.UserEntity;
import com.project01.skillineserver.enums.EmailType;
import com.project01.skillineserver.enums.ErrorCode;
import com.project01.skillineserver.enums.TokenType;
import com.project01.skillineserver.excepion.CustomException.AppException;
import com.project01.skillineserver.repository.UserDeviceRepository;
import com.project01.skillineserver.repository.UserRepository;
import com.project01.skillineserver.service.AuthService;
import com.project01.skillineserver.service.EmailService;
import com.project01.skillineserver.utils.AuthenticationUtil;
import com.project01.skillineserver.utils.CookieUtil;
import com.project01.skillineserver.utils.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Value("${url.verify-account}")
    private String verifyAccountUrl;

    private final UserDetailsService userDetailsService;
    private final SecurityUtil securityUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RedisService redisService;
    private final UserDeviceRepository userDeviceRepository;


    @Override
    public AuthResponse login(LoginRequest loginRequest,HttpServletRequest request, HttpServletResponse response) {

        CustomUserDetail user = (CustomUserDetail) userDetailsService.loadUserByUsername(loginRequest.getUsername());

        UserEntity userInDB = user.getUser();


        if(!user.isAccountNonLocked()){
            if(isAccountStillLocked(userInDB)){
                throw new AppException(ErrorCode.ACCOUNT_IS_LOCKED);
            }
        }

        if (!(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword()))) {
            increaseAttemptLogin(userInDB);
            throw new AppException(ErrorCode.PASSWORD_WRONG);
        }

        if (!user.isEnabled()) {
            throw new AppException(ErrorCode.VERIFY_ACCOUNT);
        }

        if(isPasswordExpire(userInDB)){
                throw new AppException(ErrorCode.PASSWORD_IS_EXPIRED);
        }

        resetFailedAttempts(userInDB);

        String currentDeviceId;
        if (loginRequest.getDeviceFingerprint() != null && !loginRequest.getDeviceFingerprint().isEmpty()) {
            currentDeviceId = loginRequest.getDeviceFingerprint(); // ← TỪ FRONTEND
        } else {
            currentDeviceId = createDeviceIdFromHeaders(request); // ← FALLBACK: từ headers
        }

        Optional<UserDevice> existingDevice = userDeviceRepository
                .findByUserIdAndIsActive(userInDB.getId(), true);

        if (existingDevice.isPresent()) {
            UserDevice device = existingDevice.get();

            // 3. So sánh thiết bị cũ với thiết bị hiện tại
            if (!device.getDeviceId().equals(currentDeviceId)) {
                // Khác thiết bị → Kick thiết bị cũ
                device.setActive(false);
                userDeviceRepository.save(device);

                log.info("Kicked old device for user: {}", userInDB.getId());
            } else {
                // Cùng thiết bị → Cập nhật thời gian
                device.setLastLogin(Instant.now());
                device.setIpAddress(getClientIP(request));
                userDeviceRepository.save(device);
            }
        }

        UserDevice newDevice = userDeviceRepository.findByDeviceId(currentDeviceId)
                .orElse(new UserDevice());

        newDevice.setUserId(userInDB.getId());
        newDevice.setDeviceId(currentDeviceId);
        newDevice.setIpAddress(getClientIP(request));
        newDevice.setUserAgent(request.getHeader("User-Agent"));
        newDevice.setLastLogin(Instant.now());
        newDevice.setActive(true);

        if (newDevice.getFirstLogin() == null) {
            newDevice.setFirstLogin(Instant.now());
        }

        userDeviceRepository.save(newDevice);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        String accessToken = securityUtil.generateToken(Objects
                .requireNonNull(AuthenticationUtil.getUserDetail()), TokenType.ACCESS_TOKEN, currentDeviceId);

        CookieUtil.setAccessTokenCookieHttpOnly(accessToken, response);

        String refreshToken = securityUtil.generateToken(Objects
                .requireNonNull(AuthenticationUtil.getUserDetail()), TokenType.REFRESH_TOKEN, currentDeviceId);

        CookieUtil.setRefreshTokenCookieHttpOnly(refreshToken, response);

        return AuthResponse.builder()
                .authenticated(true)
                .userId(user.getUser().getId())
                .username(user.getUsername())
                .role(user.getUser().getRole())
                .deviceId(currentDeviceId)
                .role(user.getUser().getRole())
                .avatar(user.getUser().getAvatar())
                .email(user.getUser().getEmail())
                .phone(user.getUser().getPhone())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private String createDeviceIdFromHeaders(HttpServletRequest request) {
        // Kết hợp nhiều headers hơn
        String userAgent = request.getHeader("User-Agent");
        String acceptLanguage = request.getHeader("Accept-Language");
        String acceptEncoding = request.getHeader("Accept-Encoding");
        String accept = request.getHeader("Accept");

        String rawData = userAgent + "|" + acceptLanguage + "|" + acceptEncoding + "|" + accept;

        return hash(rawData);
    }

    private String hash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error creating hash", e);
        }
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    @Override
    public boolean introspect(String token, TokenType tokenType) {

        boolean check = false;
        try {
            securityUtil.verifyToken(token,
                    tokenType);
            check = true;
        } catch (ParseException | JOSEException e) {
            throw new RuntimeException(e);
        }

        return check;
    }

    @Override
    public void refreshToken(String refreshToken, String accessToken, HttpServletResponse response) throws ParseException {

        introspect(refreshToken, TokenType.REFRESH_TOKEN);

        String username = SecurityUtil.extractUsernameByToken(refreshToken);
        String deviceId = SecurityUtil.extractDeviceIdByToken(refreshToken);
        String tokenId = SignedJWT.parse(accessToken).getJWTClaimsSet().getJWTID();

        CustomUserDetail user = (CustomUserDetail)userDetailsService.loadUserByUsername(username);

        CookieUtil.setAccessTokenCookieHttpOnly(securityUtil.generateToken(Objects
                .requireNonNull(user), TokenType.ACCESS_TOKEN, deviceId), response);

        redisService.saveData(tokenId, accessToken);
    }


    @Override
    public void createAccount(RegisterRequest registerDTO) throws IllegalAccessException {
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new AppException(ErrorCode.USER_EXITED);
        }

        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXITED);
        }

        UserEntity user = new UserEntity();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setAddress(registerDTO.getAddress());
        user.setEmail(registerDTO.getEmail());
        user.setFullname(registerDTO.getFullname());
        user.setPhone(registerDTO.getPhone());
        user.setLastTimeChangePassword(Instant.now());
        user.setRole(registerDTO.getRole());

        UserEntity userCreated = userRepository.save(user);

        sendMailForVerifyAccount(userCreated);
    }


    @Override
    public void verifyAccount(String token, Long userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (user.getCreatedAt().plus(10, ChronoUnit.MINUTES).isBefore(Instant.now())) {
            userRepository.deleteById(userId);
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public void logout(String token, HttpServletResponse response) throws ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        String tokenId = signedJWT.getJWTClaimsSet().getJWTID();
        redisService.saveData(tokenId, token);
        CookieUtil.clearAllAuthCookies(response);
    }

    @Override
    public void forgotPassword(String email) {
       boolean userExist = userRepository.existsByEmail(email);
       if(!userExist){
           throw new AppException(ErrorCode.USER_NOT_FOUND);
       }
    }

    private void increaseAttemptLogin(UserEntity user) {
        Integer countLoginFail = user.getFailedLoginAttempts();

        if(countLoginFail==null){
            countLoginFail = 0;
        }

        countLoginFail++;

        if (countLoginFail >= AppConstants.MAX_FAILED_ATTEMPTS) {
            user.setLockTime(Instant.now());
            user.setAccountNonLocked(false);
        } else {
            user.setFailedLoginAttempts(countLoginFail);
        }
        userRepository.save(user);
    }

    private void resetFailedAttempts(UserEntity user) {
        if (user.getFailedLoginAttempts() != null && user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }
    }

    private boolean isAccountStillLocked(UserEntity user) {
        boolean isCheck = true;

        Instant now = Instant.now();
        Instant unlockTime = user.getLockTime().plus(AppConstants.LOCK_TIME_DURATION, ChronoUnit.SECONDS);

        if (now.isAfter(unlockTime)) {
            user.setAccountNonLocked(true);
            user.setLockTime(null);
            user.setFailedLoginAttempts(0);
            isCheck = false;
            userRepository.save(user);
        }
        return isCheck;
    }

    private boolean isPasswordExpire(UserEntity user){
        if (user.getLastTimeChangePassword() == null) return false;
        Instant now = Instant.now();
        Instant expireTime = user.getLastTimeChangePassword()
                .plus(AppConstants.CHANGE_PASSWORD_PERIODIC, ChronoUnit.DAYS);

        if (now.isAfter(expireTime)) {
            user.setCredentialsNonExpired(false);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    private void sendMailForVerifyAccount(UserEntity user) throws IllegalAccessException {

        String tokenVerify = UUID.randomUUID().toString();

        String verifyUrl = verifyAccountUrl + "/verify" + "?userId=" + user.getId()
                + "&token=" + tokenVerify;

        emailService.verifyAccount(VerifyAccountRequest.builder()
                .token(tokenVerify)
                .linkUrl(verifyUrl)
                .userId(user.getId())
                .toEmail(user.getEmail())
                .emailType(EmailType.VERIFY_ACCOUNT)
                .toName(user.getFullname())
                .build());
    }

}
