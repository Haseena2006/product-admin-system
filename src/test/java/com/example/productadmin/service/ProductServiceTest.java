package com.example.productadmin.service;

import com.example.productadmin.dto.DashboardStats;
import com.example.productadmin.dto.ProductRequest;
import com.example.productadmin.dto.ProductResponse;
import com.example.productadmin.entity.Product;
import com.example.productadmin.exception.DuplicateProductException;
import com.example.productadmin.exception.ProductNotFoundException;
import com.example.productadmin.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private ProductRequest validRequest() {
        return new ProductRequest("  Gaming Mouse  ", "  RGB mouse ", " Electronics ",
                new BigDecimal("1499.5"), 25, 4.5);
    }

    private Product existingProduct(Long id) {
        Product p = new Product("Old Name", "Old description", "Electronics",
                new BigDecimal("100.00"), 5, 3.0);
        p.setId(id);
        return p;
    }

    @Test
    @DisplayName("create: saves a trimmed product with price scaled to 2 decimals")
    void create_success() {
        when(productRepository.existsByNameIgnoreCase("Gaming Mouse")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse response = productService.create(validRequest());

        assertThat(response.getName()).isEqualTo("Gaming Mouse");
        assertThat(response.getCategory()).isEqualTo("Electronics");
        assertThat(response.getDescription()).isEqualTo("RGB mouse");
        assertThat(response.getPrice()).isEqualByComparingTo("1499.50");
        assertThat(response.getPrice().scale()).isEqualTo(2);
        assertThat(response.getStock()).isEqualTo(25);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("create: blank description is stored as null")
    void create_blankDescriptionBecomesNull() {
        ProductRequest request = validRequest();
        request.setDescription("   ");
        when(productRepository.existsByNameIgnoreCase("Gaming Mouse")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse response = productService.create(request);

        assertThat(response.getDescription()).isNull();
    }

    @Test
    @DisplayName("create: duplicate name throws DuplicateProductException and nothing is saved")
    void create_duplicate() {
        when(productRepository.existsByNameIgnoreCase("Gaming Mouse")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(validRequest()))
                .isInstanceOf(DuplicateProductException.class)
                .hasMessageContaining("Gaming Mouse");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("getById: returns the product when it exists")
    void getById_found() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct(1L)));

        ProductResponse response = productService.getById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Old Name");
    }

    @Test
    @DisplayName("getById: throws ProductNotFoundException when the id does not exist")
    void getById_notFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("getForEdit: returns current values as a request object")
    void getForEdit_found() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct(1L)));

        ProductRequest request = productService.getForEdit(1L);

        assertThat(request.getName()).isEqualTo("Old Name");
        assertThat(request.getStock()).isEqualTo(5);
    }

    @Test
    @DisplayName("update: changes the fields of an existing product")
    void update_success() {
        Product existing = existingProduct(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.existsByNameIgnoreCaseAndIdNot("Gaming Mouse", 1L)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse response = productService.update(1L, validRequest());

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Gaming Mouse");
        assertThat(response.getStock()).isEqualTo(25);
    }

    @Test
    @DisplayName("update: throws ProductNotFoundException when the id does not exist")
    void update_notFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(99L, validRequest()))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("update: renaming to a name used by another product is rejected")
    void update_duplicateName() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct(1L)));
        when(productRepository.existsByNameIgnoreCaseAndIdNot("Gaming Mouse", 1L)).thenReturn(true);

        assertThatThrownBy(() -> productService.update(1L, validRequest()))
                .isInstanceOf(DuplicateProductException.class);

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("delete: removes an existing product")
    void delete_success() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.delete(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete: throws ProductNotFoundException when the id does not exist")
    void delete_notFound() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> productService.delete(99L))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("search: valid sort field and direction are applied, null filters become empty strings")
    void search_appliesSortAndPaging() {
        when(productRepository.search(eq(""), eq(""), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(existingProduct(1L))));

        Page<ProductResponse> result = productService.search(null, null, "price", "desc", 2, 5);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).search(eq(""), eq(""), captor.capture());
        Pageable pageable = captor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort().getOrderFor("price").getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("search: unknown sort field falls back to name, bad page/size are corrected")
    void search_sanitizesInput() {
        when(productRepository.search(eq("mouse"), eq("Electronics"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        productService.search("  mouse ", " Electronics ", "password", "sideways", -3, 5000);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).search(eq("mouse"), eq("Electronics"), captor.capture());
        Pageable pageable = captor.getValue();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(100);
        assertThat(pageable.getSort().getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(pageable.getSort().getOrderFor("password")).isNull();
    }


    @Test
    @DisplayName("stats: empty database gives zero inventory value")
    void stats_emptyDatabase() {
        when(productRepository.totalInventoryValue()).thenReturn(null);
        when(productRepository.findTop5ByOrderByCreatedAtDesc()).thenReturn(List.of());
        when(productRepository.count()).thenReturn(0L);
        when(productRepository.findDistinctCategories()).thenReturn(List.of());
        when(productRepository.countByStockLessThan(10)).thenReturn(0L);

        DashboardStats stats = productService.stats();

        assertThat(stats.getTotalProducts()).isZero();
        assertThat(stats.getInventoryValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(stats.getRecentProducts()).isEmpty();
    }

    @Test
    @DisplayName("stats: counts categories and low stock")
    void stats_withData() {
        when(productRepository.totalInventoryValue()).thenReturn(new BigDecimal("2500.00"));
        when(productRepository.findTop5ByOrderByCreatedAtDesc()).thenReturn(List.of(existingProduct(1L)));
        when(productRepository.count()).thenReturn(7L);
        when(productRepository.findDistinctCategories()).thenReturn(List.of("Books", "Furniture"));
        when(productRepository.countByStockLessThan(10)).thenReturn(3L);

        DashboardStats stats = productService.stats();

        assertThat(stats.getTotalProducts()).isEqualTo(7);
        assertThat(stats.getTotalCategories()).isEqualTo(2);
        assertThat(stats.getLowStockCount()).isEqualTo(3);
        assertThat(stats.getInventoryValue()).isEqualByComparingTo("2500.00");
        assertThat(stats.getRecentProducts()).hasSize(1);
    }
}
