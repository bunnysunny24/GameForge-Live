package com.gameforge.live.security;

import com.gameforge.live.common.BadRequestException;
import com.gameforge.live.player.Player;
import com.gameforge.live.player.PlayerRepository;
import com.gameforge.live.security.dto.AuthResponse;
import com.gameforge.live.security.dto.LoginRequest;
import com.gameforge.live.security.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private Player mockPlayer;

    @BeforeEach
    void setUp() {
        mockPlayer = Player.builder()
                .id(101L)
                .username("TestPlayer")
                .email("test@gameforge.live")
                .password("encodedPassword")
                .role(Role.ROLE_PLAYER)
                .level(1)
                .xp(0L)
                .coins(1000L)
                .gems(50)
                .build();
    }

    @Test
    void register_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .username("NewPlayer")
                .email("new@gameforge.live")
                .password("Pass123!")
                .build();

        when(playerRepository.existsByUsername("NewPlayer")).thenReturn(false);
        when(playerRepository.existsByEmail("new@gameforge.live")).thenReturn(false);
        when(passwordEncoder.encode("Pass123!")).thenReturn("encodedHash");
        when(playerRepository.save(any(Player.class))).thenReturn(mockPlayer);
        when(jwtService.generateToken(any(UserDetails.class), anyLong())).thenReturn("mock.jwt.token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals("TestPlayer", response.getUsername());
        verify(playerRepository).save(any(Player.class));
    }

    @Test
    void register_DuplicateUsername_ThrowsException() {
        RegisterRequest request = RegisterRequest.builder()
                .username("ExistingPlayer")
                .email("new@gameforge.live")
                .password("Pass123!")
                .build();

        when(playerRepository.existsByUsername("ExistingPlayer")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(playerRepository, never()).save(any());
    }

    @Test
    void login_Success() {
        LoginRequest request = LoginRequest.builder()
                .username("TestPlayer")
                .password("Pass123!")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(playerRepository.findByUsername("TestPlayer")).thenReturn(Optional.of(mockPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(mockPlayer);
        when(jwtService.generateToken(any(UserDetails.class), anyLong())).thenReturn("mock.login.token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock.login.token", response.getToken());
        assertEquals("TestPlayer", response.getUsername());
    }
}
