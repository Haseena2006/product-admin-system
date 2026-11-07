package com.example.productadmin.service;

import com.example.productadmin.dto.RegisterRequest;
import com.example.productadmin.dto.UserResponse;
import com.example.productadmin.entity.Role;
import com.example.productadmin.entity.User;
import com.example.productadmin.exception.DuplicateUserException;
import com.example.productadmin.exception.InvalidRequestException;
import com.example.productadmin.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new InvalidRequestException("confirmPassword", "Password and confirm password do not match");
        }

        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new DuplicateUserException("username", "Username is already taken");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateUserException("email", "Email is already registered");
        }

        User user = new User(username, email, passwordEncoder.encode(request.getPassword()), Role.USER);
        User saved = userRepository.save(user);
        return new UserResponse(saved.getId(), saved.getUsername(), saved.getEmail(), saved.getRole().name());
    }
}
