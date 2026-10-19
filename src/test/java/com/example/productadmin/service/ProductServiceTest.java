
package com.example.productadmin.service;

import com.example.productadmin.entity.Product;
import com.example.productadmin.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void saveProductShouldReturnSavedProduct() {
        Product product = new Product();
        product.setName("Laptop");
        product.setDescription("Test laptop");
        product.setCategory("Electronics");
        product.setPrice(50000.0);
        product.setStock(10);
        product.setRating(4);

        when(productRepository.save(product)).thenReturn(product);

        Product result = productService.saveProduct(product);

        assertNotNull(result);
        assertEquals("Laptop", result.getName());
        verify(productRepository).save(product);
    }

    @Test
    void getProductByIdShouldReturnProductWhenFound() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        Optional<Product> result = productService.getProductById(1L);

        assertTrue(result.isPresent());
        assertEquals("Laptop", result.get().getName());
    }

    @Test
    void getProductByIdShouldReturnEmptyWhenNotFound() {
        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        Optional<Product> result = productService.getProductById(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteProductShouldCallRepository() {
        productService.deleteProduct(1L);

        verify(productRepository).deleteById(1L);
    }
}

