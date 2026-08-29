package com.gameforge.live.player;

import com.gameforge.live.security.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "players", indexes = {
        @Index(name = "idx_player_username", columnList = "username", unique = true),
        @Index(name = "idx_player_email", columnList = "email", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.ROLE_PLAYER;

    @Builder.Default
    private int level = 1;

    @Builder.Default
    private long xp = 0L;

    @Builder.Default
    private long coins = 1000L;

    @Builder.Default
    private int gems = 50;

    @Builder.Default
    private String avatarUrl = "default_avatar.png";

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant lastLoginAt = Instant.now();

    @Builder.Default
    private boolean enabled = true;
}
