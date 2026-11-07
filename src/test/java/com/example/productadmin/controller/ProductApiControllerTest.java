package com.example.productadmin.controller;

import com.example.productadmin.controller.api.ProductApiController;
import com.example.productadmin.dto.ProductRequest;
import com.example.productadmin.dto.ProductResponse;
import com.example.productadmin.exception.DuplicateProductException;
import com.example.productadmin.exception.ProductNotFoundException;
import com.example.productadmin.security.SecurityConfig;
import com.example.productadmin.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductApiController.class)
@Import(SecurityConfig.class)
class ProductApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private ProductResponse sampleResponse() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 10, 0);

        return new ProductResponse(
                1L,
                "Wireless Mouse",
                "Silent clicks",
                "Electronics",
                new BigDecimal("799.00"),
                10,
                4.4,
                now,
                now
        );
    }

    private ProductRequest validRequest() {
        return new ProductRequest(
                "Wireless Mouse",
                "Silent clicks",
                "Electronics",
                new BigDecimal("799.00"),
                10,
                4.4
        );
    }

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    @Test
    @DisplayName("not logged in: GET /api/products returns 401 JSON (not an HTML login page)")
    void unauthenticated_returns401Json() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        verifyNoInteractions(productService);
    }

    @Test
    @DisplayName("not logged in: POST /api/products returns 401 JSON")
    void unauthenticated_post_returns401() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("USER: can list products with search, filter, sort and pagination")
    void user_canList() throws Exception {
        when(productService.search(
                eq("mouse"), eq("Electronics"), eq("price"),
                eq("desc"), eq(0), eq(5)
        )).thenReturn(new PageImpl<>(
                List.of(sampleResponse()), PageRequest.of(0, 5), 1
        ));

        mockMvc.perform(get("/api/products")
                        .param("q", "mouse")
                        .param("category", "Electronics")
                        .param("sort", "price")
                        .param("dir", "desc")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Wireless Mouse"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("USER: can view one product by id")
    void user_canGetById() throws Exception {
        when(productService.getById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.price").value(799.00))
                .andExpect(jsonPath("$.category").value("Electronics"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("USER: GET unknown id returns 404 with a clear message")
    void user_getUnknownId_returns404() throws Exception {
        when(productService.getById(99L))
                .thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Product not found with id: 99"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("USER: id that is not a number returns 400")
    void user_invalidIdType_returns400() throws Exception {
        mockMvc.perform(get("/api/products/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Invalid value for parameter 'id'"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("USER: POST is forbidden (403)")
    void user_cannotCreate() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRequest())))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403));

        verify(productService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("USER: PUT is forbidden (403)")
    void user_cannotUpdate() throws Exception {
        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRequest())))
                .andExpect(status().isForbidden());

        verify(productService, never()).update(any(), any());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("USER: DELETE is forbidden (403)")
    void user_cannotDelete() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isForbidden());

        verify(productService, never()).delete(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN: POST creates a product and returns 201 with Location header")
    void admin_canCreate() throws Exception {
        when(productService.create(any(ProductRequest.class)))
                .thenReturn(sampleResponse());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/products/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Wireless Mouse"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN: POST with a duplicate name returns 409")
    void admin_createDuplicate_returns409() throws Exception {
        when(productService.create(any(ProductRequest.class)))
                .thenThrow(new DuplicateProductException("Wireless Mouse"));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN: POST needs no CSRF token (stateless API) and returns 201")
    void admin_postWithoutCsrfToken_works() throws Exception {
        when(productService.create(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRequest())))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN: invalid data (blank name, price 0, negative stock, rating 9) returns 400 with field errors")
    void admin_createInvalid_returns400() throws Exception {
        ProductRequest bad = new ProductRequest(
                "   ", "x", "", new BigDecimal("0"), -5, 9.0
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.name").value("Product name is required"))
                .andExpect(jsonPath("$.fieldErrors.category").value("Category is required"))
                .andExpect(jsonPath("$.fieldErrors.price").value("Price must be greater than zero"))
                .andExpect(jsonPath("$.fieldErrors.stock").value("Stock cannot be negative"))
                .andExpect(jsonPath("$.fieldErrors.rating").value("Rating must be between 0 and 5"));

        verify(productService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN: malformed JSON returns 400")
    void admin_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"Mouse\", \"price\": \"not-a-number\" "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN: PUT updates a product and returns 200")
    void admin_canUpdate() throws Exception {
        when(productService.update(eq(1L), any(ProductRequest.class)))
                .thenReturn(sampleResponse());

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Wireless Mouse"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN: PUT on unknown id returns 404")
    void admin_updateUnknown_returns404() throws Exception {
        when(productService.update(eq(99L), any(ProductRequest.class)))
                .thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(put("/api/products/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN: PUT with invalid data returns 400")
    void admin_updateInvalid_returns400() throws Exception {
        ProductRequest bad = new ProductRequest(
                "Mouse", null, "Electronics", new BigDecimal("-1"), 1, 4.0
        );

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.price")
                        .value("Price must be greater than zero"));

        verify(productService, never()).update(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN: DELETE returns 204 No Content")
    void admin_canDelete() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());

        verify(productService).delete(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN: DELETE on unknown id returns 404")
    void admin_deleteUnknown_returns404() throws Exception {
        doThrow(new ProductNotFoundException(99L))
                .when(productService).delete(99L);

        mockMvc.perform(delete("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Product not found with id: 99"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN: can also use the read endpoints")
    void admin_canList() throws Exception {
        when(productService.search(
                any(), any(), any(), any(), eq(0), eq(10)
        )).thenReturn(new PageImpl<>(
                List.of(sampleResponse()), PageRequest.of(0, 10), 1
        ));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }
}
