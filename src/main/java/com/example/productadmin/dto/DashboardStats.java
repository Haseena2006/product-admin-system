package com.example.productadmin.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardStats {

    private final long totalProducts;
    private final long totalCategories;
    private final long lowStockCount;
    private final BigDecimal inventoryValue;
    private final List<ProductResponse> recentProducts;

    public DashboardStats(long totalProducts, long totalCategories, long lowStockCount,
                          BigDecimal inventoryValue, List<ProductResponse> recentProducts) {
        this.totalProducts = totalProducts;
        this.totalCategories = totalCategories;
        this.lowStockCount = lowStockCount;
        this.inventoryValue = inventoryValue;
        this.recentProducts = recentProducts;
    }

    public long getTotalProducts() { return totalProducts; }
    public long getTotalCategories() { return totalCategories; }
    public long getLowStockCount() { return lowStockCount; }
    public BigDecimal getInventoryValue() { return inventoryValue; }
    public List<ProductResponse> getRecentProducts() { return recentProducts; }
}
