package com.example.productadmin.controller;

import com.example.productadmin.controller.web.ProductWebController;
import com.example.productadmin.dto.ProductRequest;
import com.example.productadmin.dto.ProductResponse;
import com.example.productadmin.exception.DuplicateProductException;
import com.example.productadmin.security.SecurityConfig;
import com.example.productadmin.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ProductWebController.class)
@Import(SecurityConfig.class)
class ProductWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    private ProductResponse sample() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 10, 0);
        return new ProductResponse(1L, "Wireless Mouse", "Silent clicks", "Electronics",
                new BigDecimal("799.00"), 10, 4.4, now, now);
    }

    @Test
    @DisplayName("not logged in: /products redirects to the login page")
    void unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("USER: product list page renders")
    void user_canOpenList() throws Exception {
        when(productService.search(any(), any(), any(), any(), any(Integer.class), any(Integer.class)))
                .thenReturn(new PageImpl<>(List.of(sample()), PageRequest.of(0, 8), 1));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("product-list"))
                .andExpect(model().attributeExists("page", "categories"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("USER: product details page renders")
    void user_canOpenDetails() throws Exception {
        when(productService.getById(1L)).thenReturn(sample());

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("product-details"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("USER: cannot open the add-product form (403)")
    void user_cannotOpenNewForm() throws Exception {
        mockMvc.perform(get("/products/new"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("USER: cannot open the edit form (403)")
    void user_cannotOpenEditForm() throws Exception {
        mockMvc.perform(get("/products/1/edit"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("USER: cannot create, update or delete (403)")
    void user_cannotModify() throws Exception {
        mockMvc.perform(post("/products").with(csrf())
                        .param("name", "X").param("category", "Y")
                        .param("price", "10").param("stock", "1").param("rating", "4"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/products/1").with(csrf())
                        .param("name", "X").param("category", "Y")
                        .param("price", "10").param("stock", "1").param("rating", "4"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/products/1/delete").with(csrf()))
                .andExpect(status().isForbidden());

        verify(productService, never()).create(any());
        verify(productService, never()).update(any(), any());
        verify(productService, never()).delete(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN: add-product form renders")
    void admin_canOpenNewForm() throws Exception {
        mockMvc.perform(get("/products/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("product-form"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN: valid form creates the product and redirects to the list")
    void admin_createRedirects() throws Exception {
        when(productService.create(any(ProductRequest.class))).thenReturn(sample());

        mockMvc.perform(post("/products").with(csrf())
                        .param("name", "Wireless Mouse").param("category", "Electronics")
                        .param("price", "799.00").param("stock", "10").param("rating", "4.4"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));

        verify(productService).create(any(ProductRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN: invalid form is shown again with errors and nothing is saved")
    void admin_createInvalid_showsFormAgain() throws Exception {
        mockMvc.perform(post("/products").with(csrf())
                        .param("name", "").param("category", "")
                        .param("price", "-5").param("stock", "-1").param("rating", "9"))
                .andExpect(status().isOk())
                .andExpect(view().name("product-form"))
                .andExpect(model().attributeHasFieldErrors("product", "name", "category", "price", "stock", "rating"));

        verify(productService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN: duplicate product name is shown as a field error")
    void admin_createDuplicate_showsFieldError() throws Exception {
        when(productService.create(any(ProductRequest.class)))
                .thenThrow(new DuplicateProductException("Wireless Mouse"));

        mockMvc.perform(post("/products").with(csrf())
                        .param("name", "Wireless Mouse").param("category", "Electronics")
                        .param("price", "799.00").param("stock", "10").param("rating", "4.4"))
                .andExpect(status().isOk())
                .andExpect(view().name("product-form"))
                .andExpect(model().attributeHasFieldErrors("product", "name"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN: delete redirects back to the list")
    void admin_deleteRedirects() throws Exception {
        mockMvc.perform(post("/products/1/delete").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));

        verify(productService).delete(1L);
    }
}
