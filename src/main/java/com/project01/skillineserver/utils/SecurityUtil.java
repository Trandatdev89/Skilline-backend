package com.project01.skillineserver.utils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import com.project01.skillineserver.config.CustomUserDetail;
import com.project01.skillineserver.entity.UserDevice;
import com.project01.skillineserver.enums.ErrorCode;
import com.project01.skillineserver.enums.TokenType;
import com.project01.skillineserver.excepion.CustomException.AppException;
import com.project01.skillineserver.repository.UserDeviceRepository;
import com.project01.skillineserver.service.Impl.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SecurityUtil {


    @Autowired
    @Qualifier("accessTokenEncoder")
    @Lazy
    private JwtEncoder accessTokenEncoder;

    @Autowired
    @Qualifier("refreshTokenEncoder")
    @Lazy
    private JwtEncoder refreshTokenEncoder;

    @Value("${jwt.secretKeyAccess}")
    private String secretKeyAccess;

    @Value("${jwt.secretKeyRefresh}")
    private String secretKeyRefresh;

    @Value("${jwt.expirationAccess}")
    private long expirationAccess;

    @Value("${jwt.expirationRefresh}")
    private long expirationRefresh;

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserDeviceRepository userDeviceRepository;

    public String generateToken(CustomUserDetail customUserDetail, TokenType tokenType,String deviceId) {
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();


        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                .subject(customUserDetail.getUser().getId().toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(tokenType.equals(TokenType.ACCESS_TOKEN) ? expirationAccess : expirationRefresh, ChronoUnit.SECONDS))
                .claims(setClaims(customUserDetail,tokenType,deviceId))
                .issuer(customUserDetail.getUsername())
                .id(UUID.randomUUID().toString())
                .build();

        String tokenCreated = "";

        if (tokenType.equals(TokenType.ACCESS_TOKEN)) {
            tokenCreated = accessTokenEncoder.encode(JwtEncoderParameters.from(jwsHeader, jwtClaimsSet)).getTokenValue();
        } else if (tokenType.equals(TokenType.REFRESH_TOKEN)) {
            tokenCreated = refreshTokenEncoder.encode(JwtEncoderParameters.from(jwsHeader, jwtClaimsSet)).getTokenValue();
        }

        return tokenCreated;
    }


    public SignedJWT verifyToken(String token, TokenType tokenType) throws ParseException, JOSEException {
        if (!StringUtils.hasText(token)) {
            throw new AppException(ErrorCode.TOKEN_IS_BLANK);
        }

        SignedJWT signedJWT = SignedJWT.parse(token);
        JWSVerifier verifier = new MACVerifier(tokenType.equals(TokenType.ACCESS_TOKEN)
                ? secretKey() : secretRefreshKey());
        boolean verified = signedJWT.verify(verifier);
        String tokenId = signedJWT.getJWTClaimsSet().getJWTID();
        String deviceIdFromToken = signedJWT.getJWTClaimsSet().getClaim("deviceId").toString();

        if (!(verified && signedJWT.getJWTClaimsSet().getExpirationTime().after(new Date()))) {
            log.info("Token is not verify or expire : {}", token);
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        if (redisService.existsKey(tokenId)) {
            log.info("Account is logout with tokenId,token : {} {}", tokenId, token);
            throw new AppException(ErrorCode.ACCOUNT_IS_LOGOUT);
        }

        Optional<UserDevice> device = userDeviceRepository.findByDeviceId(deviceIdFromToken);
        if(device.isEmpty() || !device.get().isActive()){
            log.info("Account of you login on device other, device id not found: {}", device);
            throw new AppException(ErrorCode.ACCOUNT_LOGINED);
        }

        return signedJWT;
    }

    public SecretKey secretKey() {
        byte[] bytes = secretKeyAccess.getBytes();
        return new SecretKeySpec(bytes, 0, bytes.length, MacAlgorithm.HS256.getName());
    }

    public SecretKey secretRefreshKey() {
        byte[] bytes = secretKeyRefresh.getBytes();
        return new SecretKeySpec(bytes, 0, bytes.length, MacAlgorithm.HS256.getName());
    }

    public String getAuthorities(CustomUserDetail customUserDetail) {
        return customUserDetail.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
    }

    private Consumer<Map<String, Object>> setClaims(CustomUserDetail customUserDetail,TokenType tokenType,String deviceId) {
        return stringObjectMap -> {
            stringObjectMap.put("scope", getAuthorities(customUserDetail));
            stringObjectMap.put("loginAt", LocalDateTime.now().toString());
            stringObjectMap.put("typeToken", tokenType);
            if(deviceId!=null){
                stringObjectMap.put("deviceId", deviceId);
            }
        };
    }

    public static String extractUsernameByToken(String token) throws ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        return signedJWT.getJWTClaimsSet().getIssuer();
    }
}