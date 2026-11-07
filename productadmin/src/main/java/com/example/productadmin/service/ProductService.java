package com.example.productadmin.service;

import com.example.productadmin.dto.DashboardStats;
import com.example.productadmin.dto.ProductRequest;
import com.example.productadmin.dto.ProductResponse;
import com.example.productadmin.entity.Product;
import com.example.productadmin.exception.DuplicateProductException;
import com.example.productadmin.exception.ProductNotFoundException;
import com.example.productadmin.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

@Service
public class ProductService {

    private static final Set<String> SORT_FIELDS = Set.of("name", "price", "rating", "stock", "createdAt");
    private static final int LOW_STOCK_LIMIT = 10;
    private static final int MAX_PAGE_SIZE = 50;

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String q, String category, String sortBy, String direction,
                                        int page, int size) {
        String field = SORT_FIELDS.contains(sortBy) ? sortBy : "name";
        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(dir, field).and(Sort.by("id")));
        return productRepository.search(clean(q), clean(category), pageable).map(ProductResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return ProductResponse.fromEntity(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public ProductRequest getForEdit(Long id) {
        Product p = findOrThrow(id);
        return new ProductRequest(p.getName(), p.getDescription(), p.getCategory(),
                p.getPrice(), p.getStock(), p.getRating());
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        String name = request.getName().trim();
        if (productRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateProductException(name);
        }
        Product product = new Product();
        apply(product, request, name);
        return ProductResponse.fromEntity(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findOrThrow(id);
        String name = request.getName().trim();
        if (productRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateProductException(name);
        }
        apply(product, request, name);
        return ProductResponse.fromEntity(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<String> categories() {
        return productRepository.findDistinctCategories();
    }

    @Transactional(readOnly = true)
    public DashboardStats stats() {
        BigDecimal value = productRepository.totalInventoryValue();
        List<ProductResponse> recent = productRepository.findTop5ByOrderByCreatedAtDesc()
                .stream().map(ProductResponse::fromEntity).toList();
        return new DashboardStats(
                productRepository.count(),
                productRepository.findDistinctCategories().size(),
                productRepository.countByStockLessThan(LOW_STOCK_LIMIT),
                value == null ? BigDecimal.ZERO : value,
                recent);
    }

    private Product findOrThrow(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    private void apply(Product product, ProductRequest request, String name) {
        product.setName(name);
        String description = request.getDescription();
        product.setDescription(description == null || description.isBlank() ? null : description.trim());
        product.setCategory(request.getCategory().trim());
        product.setPrice(request.getPrice().setScale(2, RoundingMode.HALF_UP));
        product.setStock(request.getStock());
        product.setRating(request.getRating());
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
