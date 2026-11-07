package com.example.productadmin.controller;

import com.example.productadmin.controller.api.AuthApiController;
import com.example.productadmin.dto.RegisterRequest;
import com.example.productadmin.dto.UserResponse;
import com.example.productadmin.exception.DuplicateUserException;
import com.example.productadmin.security.SecurityConfig;
import com.example.productadmin.service.RegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthApiController.class)
@Import(SecurityConfig.class)
class AuthApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegistrationService registrationService;

    @Test
    @DisplayName("GET /api/csrf no longer exists: the API is stateless and needs no CSRF token")
    void csrfEndpointIsGone() throws Exception {
        mockMvc.perform(get("/api/csrf"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/register: valid data returns 201 and no password in the response")
    void register_success() throws Exception {
        when(registrationService.register(any(RegisterRequest.class)))
                .thenReturn(new UserResponse(5L, "haseena", "haseena@example.com", "USER"));

        RegisterRequest body = new RegisterRequest("haseena", "haseena@example.com", "Secret123", "Secret123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("haseena"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/auth/register: invalid data returns 400 with field errors")
    void register_invalid() throws Exception {
        RegisterRequest body = new RegisterRequest("a", "not-an-email", "weak", "");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.username").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists())
                .andExpect(jsonPath("$.fieldErrors.confirmPassword").exists());

        verify(registrationService, never()).register(any());
    }

    @Test
    @DisplayName("POST /api/auth/register: duplicate username returns 409")
    void register_duplicate() throws Exception {
        when(registrationService.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicateUserException("username", "Username is already taken"));

        RegisterRequest body = new RegisterRequest("haseena", "haseena@example.com", "Secret123", "Secret123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.fieldErrors.username").value("Username is already taken"));
    }

    @Test
    @DisplayName("POST /api/auth/register needs no CSRF token (stateless API)")
    void register_withoutCsrfToken_works() throws Exception {
        when(registrationService.register(any(RegisterRequest.class)))
                .thenReturn(new UserResponse(6L, "haseena", "haseena@example.com", "USER"));
        RegisterRequest body = new RegisterRequest("haseena", "haseena@example.com", "Secret123", "Secret123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("GET /api/auth/me without login returns 401 JSON")
    void me_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /api/auth/me returns the logged-in user and roles")
    void me_authenticated() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
    }
}
