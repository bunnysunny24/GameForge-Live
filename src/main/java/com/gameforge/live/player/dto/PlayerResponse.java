package com.gameforge.live.player.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerResponse {
    private Long id;
    private String username;
    private String email;
    private String role;
    private int level;
    private long xp;
    private long xpToNextLevel;
    private long coins;
    private int gems;
    private String avatarUrl;
    private Instant createdAt;
    private Instant lastLoginAt;
}
