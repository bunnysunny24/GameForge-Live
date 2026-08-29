package com.gameforge.live.security;

import com.gameforge.live.player.Player;
import com.gameforge.live.player.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final PlayerRepository playerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Player player = playerRepository.findByUsername(username)
                .or(() -> playerRepository.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("Player not found with username or email: " + username));
        return new CustomUserDetails(player);
    }
}
