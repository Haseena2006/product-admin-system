package com.example.productadmin.dto;

import com.example.productadmin.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductResponse {

    private final Long id;
    private final String name;
    private final String description;
    private final String category;
    private final BigDecimal price;
    private final Integer stock;
    private final Double rating;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ProductResponse(Long id, String name, String description, String category, BigDecimal price,
                           Integer stock, Double rating, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.rating = rating;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ProductResponse fromEntity(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getCategory(),
                p.getPrice(), p.getStock(), p.getRating(), p.getCreatedAt(), p.getUpdatedAt());
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public BigDecimal getPrice() { return price; }
    public Integer getStock() { return stock; }
    public Double getRating() { return rating; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
