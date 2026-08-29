package com.gameforge.live.security;

import com.gameforge.live.common.BadRequestException;
import com.gameforge.live.player.Player;
import com.gameforge.live.player.PlayerRepository;
import com.gameforge.live.security.dto.AuthResponse;
import com.gameforge.live.security.dto.LoginRequest;
import com.gameforge.live.security.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (playerRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username '" + request.getUsername() + "' is already taken");
        }
        if (playerRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email '" + request.getEmail() + "' is already registered");
        }

        Player player = Player.builder()
                .username(request.getUsername().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_PLAYER)
                .level(1)
                .xp(0L)
                .coins(1000L)
                .gems(50)
                .avatarUrl("avatar_starter.png")
                .createdAt(Instant.now())
                .lastLoginAt(Instant.now())
                .enabled(true)
                .build();

        Player savedPlayer = playerRepository.save(player);
        log.info("Registered new player: ID={}, username={}", savedPlayer.getId(), savedPlayer.getUsername());

        CustomUserDetails userDetails = new CustomUserDetails(savedPlayer);
        String token = jwtService.generateToken(userDetails, savedPlayer.getId());

        return buildAuthResponse(savedPlayer, token);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        Player player = playerRepository.findByUsername(request.getUsername())
                .or(() -> playerRepository.findByEmail(request.getUsername()))
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        player.setLastLoginAt(Instant.now());
        playerRepository.save(player);

        CustomUserDetails userDetails = new CustomUserDetails(player);
        String token = jwtService.generateToken(userDetails, player.getId());
        log.info("Player logged in: ID={}, username={}", player.getId(), player.getUsername());

        return buildAuthResponse(player, token);
    }

    private AuthResponse buildAuthResponse(Player player, String token) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .playerId(player.getId())
                .username(player.getUsername())
                .email(player.getEmail())
                .role(player.getRole().name())
                .level(player.getLevel())
                .coins(player.getCoins())
                .gems(player.getGems())
                .build();
    }
}
