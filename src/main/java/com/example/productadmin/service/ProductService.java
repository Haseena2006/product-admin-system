
package com.example.productadmin.service;

import com.example.productadmin.entity.Product;
import com.example.productadmin.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Page<Product> getProducts(
            String search,
            String category,
            String sortBy,
            String direction,
            int page,
            int size) {

        String safeSearch = search == null ? "" : search.trim();
        String safeCategory = category == null ? "" : category.trim();

        int safePage = Math.max(page, 0);
        int safeSize = (size == 10 || size == 20 || size == 50) ? size : 10;

        String safeSortBy = switch (sortBy == null ? "name" : sortBy) {
            case "price", "stock", "rating", "name" -> sortBy;
            default -> "name";
        };

        Sort.Direction sortDirection =
                "desc".equalsIgnoreCase(direction)
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(sortDirection, safeSortBy)
        );

        if (!safeSearch.isEmpty() && !safeCategory.isEmpty()) {
            return productRepository
                    .findByNameContainingIgnoreCaseAndCategoryIgnoreCase(
                            safeSearch, safeCategory, pageable);
        }

        if (!safeSearch.isEmpty()) {
            return productRepository
                    .findByNameContainingIgnoreCase(safeSearch, pageable);
        }

        if (!safeCategory.isEmpty()) {
            return productRepository
                    .findByCategoryIgnoreCase(safeCategory, pageable);
        }

        return productRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}


