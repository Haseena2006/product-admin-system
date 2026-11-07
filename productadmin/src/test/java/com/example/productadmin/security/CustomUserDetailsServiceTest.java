package com.example.productadmin.security;

import com.example.productadmin.entity.Role;
import com.example.productadmin.entity.User;
import com.example.productadmin.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    @DisplayName("loadUserByUsername: ADMIN user gets ROLE_ADMIN and keeps the stored hash")
    void loadAdmin() {
        User admin = new User("admin", "admin@example.com", "$2a$10$hash", Role.ADMIN);
        when(userRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(admin));

        UserDetails details = service.loadUserByUsername("admin");

        assertThat(details.getUsername()).isEqualTo("admin");
        assertThat(details.getPassword()).isEqualTo("$2a$10$hash");
        assertThat(details.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
        assertThat(details.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("loadUserByUsername: normal user gets ROLE_USER")
    void loadUser() {
        User user = new User("user", "user@example.com", "$2a$10$hash", Role.USER);
        when(userRepository.findByUsernameIgnoreCase("user")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("user");

        assertThat(details.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("loadUserByUsername: disabled account is reported as disabled")
    void loadDisabledUser() {
        User user = new User("blocked", "blocked@example.com", "$2a$10$hash", Role.USER);
        user.setEnabled(false);
        when(userRepository.findByUsernameIgnoreCase("blocked")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("blocked");

        assertThat(details.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("loadUserByUsername: unknown user throws UsernameNotFoundException")
    void loadUnknownUser() {
        when(userRepository.findByUsernameIgnoreCase("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
