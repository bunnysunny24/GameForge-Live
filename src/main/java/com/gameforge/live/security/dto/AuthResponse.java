package com.gameforge.live.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType;
    private Long playerId;
    private String username;
    private String email;
    private String role;
    private int level;
    private long coins;
    private int gems;
}
