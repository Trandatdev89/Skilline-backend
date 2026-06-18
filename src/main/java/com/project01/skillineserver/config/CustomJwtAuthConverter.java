package com.project01.skillineserver.config;

import com.project01.skillineserver.entity.UserEntity;
import com.project01.skillineserver.enums.ErrorCode;
import com.project01.skillineserver.excepion.CustomException.AppException;
import com.project01.skillineserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomJwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository userRepository;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {

        UserEntity user = userRepository.findById(Long.parseLong(jwt.getSubject()))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        CustomUserDetail userDetail = new CustomUserDetail(user);

        List<GrantedAuthority> authorities = extractAuthorities(jwt);

        return new UsernamePasswordAuthenticationToken(userDetail,null,authorities);
    }

    private List<GrantedAuthority> extractAuthorities(Jwt jwt) {
        String role = jwt.getClaimAsString("scope");
        if (role == null) return List.of();

        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
