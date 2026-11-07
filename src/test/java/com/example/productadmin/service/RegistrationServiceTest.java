package com.example.productadmin.service;

import com.example.productadmin.dto.RegisterRequest;
import com.example.productadmin.dto.UserResponse;
import com.example.productadmin.entity.Role;
import com.example.productadmin.entity.User;
import com.example.productadmin.exception.DuplicateUserException;
import com.example.productadmin.exception.InvalidRequestException;
import com.example.productadmin.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegistrationService registrationService;

    private RegisterRequest request() {
        return new RegisterRequest("  Haseena ", "  Haseena@Example.COM ", "Secret123", "Secret123");
    }

    @Test
    @DisplayName("register: creates a USER with encoded password and lower-case email")
    void register_success() {
        when(userRepository.existsByUsernameIgnoreCase("Haseena")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("haseena@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret123")).thenReturn("ENCODED_HASH");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = registrationService.register(request());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getPassword()).isEqualTo("ENCODED_HASH");
        assertThat(saved.getPassword()).isNotEqualTo("Secret123");
        assertThat(saved.getRole()).isEqualTo(Role.USER);
        assertThat(saved.getEmail()).isEqualTo("haseena@example.com");
        assertThat(saved.getUsername()).isEqualTo("Haseena");
        assertThat(response.getRole()).isEqualTo("USER");
    }

    @Test
    @DisplayName("register: password and confirm password must match")
    void register_passwordMismatch() {
        RegisterRequest request = request();
        request.setConfirmPassword("Different123");

        assertThatThrownBy(() -> registrationService.register(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("do not match");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("register: duplicate username is rejected")
    void register_duplicateUsername() {
        when(userRepository.existsByUsernameIgnoreCase("Haseena")).thenReturn(true);

        assertThatThrownBy(() -> registrationService.register(request()))
                .isInstanceOfSatisfying(DuplicateUserException.class,
                        ex -> assertThat(ex.getField()).isEqualTo("username"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("register: duplicate email is rejected")
    void register_duplicateEmail() {
        when(userRepository.existsByUsernameIgnoreCase("Haseena")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("haseena@example.com")).thenReturn(true);

        assertThatThrownBy(() -> registrationService.register(request()))
                .isInstanceOfSatisfying(DuplicateUserException.class,
                        ex -> assertThat(ex.getField()).isEqualTo("email"));

        verify(userRepository, never()).save(any(User.class));
    }
}
