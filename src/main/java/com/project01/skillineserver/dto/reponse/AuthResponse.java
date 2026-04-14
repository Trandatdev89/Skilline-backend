package com.project01.skillineserver.dto.reponse;

import com.project01.skillineserver.enums.Role;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private boolean authenticated;
    private String username;
    private Long userId;
    private Role role;
    private String deviceId;
    private String avatar;
    private String accessToken;
    private String refreshToken;
}
