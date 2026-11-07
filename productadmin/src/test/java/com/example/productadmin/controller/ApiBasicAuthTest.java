package com.example.productadmin.controller;

import com.example.productadmin.controller.api.ProductApiController;
import com.example.productadmin.dto.ProductRequest;
import com.example.productadmin.dto.ProductResponse;
import com.example.productadmin.security.SecurityConfig;
import com.example.productadmin.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ProductApiController.class)
@Import({SecurityConfig.class, ApiBasicAuthTest.TestUsers.class})
class ApiBasicAuthTest {

    @TestConfiguration
    static class TestUsers {
        @Bean
        UserDetailsService userDetailsService(PasswordEncoder encoder) {
            return new InMemoryUserDetailsManager(
                    User.withUsername("admin").password(encoder.encode("Admin@123")).roles("ADMIN").build(),
                    User.withUsername("user").password(encoder.encode("User@123")).roles("USER").build());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private ProductResponse sample() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 10, 0);
        return new ProductResponse(1L, "Wireless Mouse", "Silent clicks", "Electronics",
                new BigDecimal("799.00"), 10, 4.4, now, now);
    }

    private String validBody() throws Exception {
        return objectMapper.writeValueAsString(new ProductRequest("Wireless Mouse", "Silent clicks",
                "Electronics", new BigDecimal("799.00"), 10, 4.4));
    }

    @Test
    @DisplayName("no credentials: 401 JSON with a WWW-Authenticate: Basic header")
    void noCredentials_returns401() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", org.hamcrest.Matchers.startsWith("Basic")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401));
        verifyNoInteractions(productService);
    }

    @Test
    @DisplayName("wrong password: 401 JSON")
    void wrongPassword_returns401() throws Exception {
        mockMvc.perform(get("/api/products").with(httpBasic("admin", "wrong-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("USER with Basic Auth can read (GET /api/products/1 returns 200)")
    void userBasic_canRead() throws Exception {
        when(productService.getById(1L)).thenReturn(sample());

        mockMvc.perform(get("/api/products/1").with(httpBasic("user", "User@123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Wireless Mouse"));
    }

    @Test
    @DisplayName("USER with Basic Auth cannot create: 403 JSON")
    void userBasic_cannotCreate() throws Exception {
        mockMvc.perform(post("/api/products").with(httpBasic("user", "User@123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        verifyNoInteractions(productService);
    }

    @Test
    @DisplayName("USER with Basic Auth cannot delete: 403 JSON")
    void userBasic_cannotDelete() throws Exception {
        mockMvc.perform(delete("/api/products/1").with(httpBasic("user", "User@123")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(productService);
    }

    @Test
    @DisplayName("ADMIN with Basic Auth can create without any CSRF token: 201")
    void adminBasic_canCreate_withoutCsrf() throws Exception {
        when(productService.create(any())).thenReturn(sample());

        mockMvc.perform(post("/api/products").with(httpBasic("admin", "Admin@123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("the API does not create a session or cookie (stateless)")
    void api_isStateless() throws Exception {
        when(productService.getById(1L)).thenReturn(sample());

        mockMvc.perform(get("/api/products/1").with(httpBasic("admin", "Admin@123")))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Set-Cookie"));
    }
}
