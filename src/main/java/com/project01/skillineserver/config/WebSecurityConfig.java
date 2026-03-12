package com.project01.skillineserver.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.project01.skillineserver.entity.UserEntity;
import com.project01.skillineserver.enums.ErrorCode;
import com.project01.skillineserver.enums.TokenType;
import com.project01.skillineserver.excepion.CustomException.AppException;
import com.project01.skillineserver.repository.UserRepository;
import com.project01.skillineserver.service.AuthService;
import com.project01.skillineserver.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    private static final String[] PUBLIC_ENTRYPOINT = {"/auth/**", "/api/file/**", "/chat/**",
            "/vnpay-payment/**", "/api/lecture/**", "/api/course/**", "/api/push/**", "/api/test", "/ws/**"};

    @Lazy
    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Lazy
    private AuthService authService;

    @Autowired
    private CookieBearerTokenResolver cookieBearerTokenResolver;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            UserEntity userEntity = userRepository.findByUsername(username)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            return new CustomUserDetail(userEntity);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookiePath("/");
        repository.setCookieMaxAge(7 * 24 * 60 * 60);
        return repository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, CustomJwtAuthConverter customJwtAuthConverter, CsrfTokenRepository csrfTokenRepository) throws Exception {

        return httpSecurity
                .csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy())
                        .ignoringRequestMatchers("/auth/**", "/api/file/**", "/ws/**"))
                .addFilterBefore(new CsrfValidationFilter(csrfTokenRepository), CsrfFilter.class)
                .authorizeHttpRequests(http -> http
                        .requestMatchers(HttpMethod.GET, "/product/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/category/pagination").permitAll()
                        .requestMatchers(HttpMethod.GET, "/review/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/image/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/lecture/**").permitAll()
                        .requestMatchers(PUBLIC_ENTRYPOINT).permitAll()
                        .anyRequest().authenticated())
                .formLogin(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(oauth -> oauth
                        .bearerTokenResolver(cookieBearerTokenResolver)
                        .accessDeniedHandler(new JwtAccessDeniedEntryPoint())
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                        .jwt(config -> config
                                .jwtAuthenticationConverter(customJwtAuthConverter)
                                .decoder(accessTokenDecoder())))
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(new JwtAccessDeniedEntryPoint())
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("http://localhost:5173");
        corsConfiguration.addAllowedOrigin("http://localhost:63342");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(urlBasedCorsConfigurationSource);
    }

    @Bean
    @Qualifier("accessTokenEncoder")
    public JwtEncoder accessTokenEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(securityUtil.secretKey()));
    }

    @Bean
    @Qualifier("accessTokenDecoder")
    public JwtDecoder accessTokenDecoder() {
        NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder
                .withSecretKey(securityUtil.secretKey()).macAlgorithm(MacAlgorithm.HS256).build();
        return token -> {
            try {
                var responseToken = authService.introspect(token, TokenType.ACCESS_TOKEN);
                if (!responseToken) {
                    throw new AppException(ErrorCode.INVALID_TOKEN);
                }
                return nimbusJwtDecoder.decode(token);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                throw e;
            }
        };
    }

    @Bean
    @Qualifier("refreshTokenEncoder")
    public JwtEncoder refreshTokenEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(securityUtil.secretRefreshKey()));
    }

    @Bean
    @Qualifier("refreshTokenDecoder")
    public JwtDecoder refreshTokenDecoder() {
        NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(securityUtil.secretRefreshKey()).macAlgorithm(MacAlgorithm.HS256).build();
        return nimbusJwtDecoder::decode;
    }
}
