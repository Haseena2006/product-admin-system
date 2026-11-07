package com.example.productadmin.repository;

import com.example.productadmin.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
            select p from Product p
            where (:q = '' or lower(p.name) like lower(concat('%', :q, '%')))
              and (:category = '' or lower(p.category) = lower(:category))
            """)
    Page<Product> search(@Param("q") String q, @Param("category") String category, Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("select distinct p.category from Product p order by p.category")
    List<String> findDistinctCategories();

    @Query("select sum(p.price * p.stock) from Product p")
    BigDecimal totalInventoryValue();

    long countByStockLessThan(int stock);

    List<Product> findTop5ByOrderByCreatedAtDesc();
}
