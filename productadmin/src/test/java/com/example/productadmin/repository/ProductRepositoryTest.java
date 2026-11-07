package com.example.productadmin.repository;

import com.example.productadmin.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.save(product("Wireless Mouse", "Electronics", "799.00", 10));
        productRepository.save(product("Mechanical Keyboard", "Electronics", "3499.00", 0));
        productRepository.save(product("Office Chair", "Furniture", "7999.00", 4));
        productRepository.save(product("Clean Code", "Books", "699.00", 50));
    }

    private Product product(String name, String category, String price, int stock) {
        return new Product(name, "desc", category, new BigDecimal(price), stock, 4.0);
    }

    @Test
    @DisplayName("save: createdAt and updatedAt are filled automatically")
    void save_setsTimestamps() {
        Product saved = productRepository.save(product("Webcam", "Accessories", "2299.00", 3));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("search: name search is case-insensitive and matches part of the name")
    void search_byName() {
        Page<Product> result = productRepository.search("MOUSE", "", PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Product::getName).containsExactly("Wireless Mouse");
    }

    @Test
    @DisplayName("search: empty filters return everything")
    void search_noFilters() {
        Page<Product> result = productRepository.search("", "", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(4);
    }

    @Test
    @DisplayName("search: category filter returns only that category")
    void search_byCategory() {
        Page<Product> result = productRepository.search("", "electronics", PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Product::getName)
                .containsExactlyInAnyOrder("Wireless Mouse", "Mechanical Keyboard");
    }

    @Test
    @DisplayName("search: name and category can be combined")
    void search_nameAndCategory() {
        Page<Product> result = productRepository.search("key", "Electronics", PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Product::getName).containsExactly("Mechanical Keyboard");
    }

    @Test
    @DisplayName("search: no match gives an empty page")
    void search_noMatch() {
        Page<Product> result = productRepository.search("does-not-exist", "", PageRequest.of(0, 10));

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("search: sorting by price descending")
    void search_sortByPriceDesc() {
        Page<Product> result = productRepository.search("", "",
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "price")));

        assertThat(result.getContent()).extracting(Product::getName)
                .containsExactly("Office Chair", "Mechanical Keyboard", "Wireless Mouse", "Clean Code");
    }

    @Test
    @DisplayName("search: sorting by name ascending")
    void search_sortByNameAsc() {
        Page<Product> result = productRepository.search("", "",
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")));

        assertThat(result.getContent()).extracting(Product::getName)
                .containsExactly("Clean Code", "Mechanical Keyboard", "Office Chair", "Wireless Mouse");
    }

    @Test
    @DisplayName("search: pagination splits results into pages")
    void search_pagination() {
        Page<Product> first = productRepository.search("", "",
                PageRequest.of(0, 3, Sort.by("name")));
        Page<Product> second = productRepository.search("", "",
                PageRequest.of(1, 3, Sort.by("name")));

        assertThat(first.getContent()).hasSize(3);
        assertThat(first.getTotalPages()).isEqualTo(2);
        assertThat(second.getContent()).hasSize(1);
        assertThat(second.isLast()).isTrue();
    }

    @Test
    @DisplayName("existsByNameIgnoreCase: ignores upper/lower case")
    void existsByName() {
        assertThat(productRepository.existsByNameIgnoreCase("wireless mouse")).isTrue();
        assertThat(productRepository.existsByNameIgnoreCase("Unknown")).isFalse();
    }

    @Test
    @DisplayName("existsByNameIgnoreCaseAndIdNot: the product itself is not a duplicate of itself")
    void existsByNameAndIdNot() {
        Product mouse = productRepository.search("Wireless", "", PageRequest.of(0, 1)).getContent().get(0);

        assertThat(productRepository.existsByNameIgnoreCaseAndIdNot("Wireless Mouse", mouse.getId())).isFalse();
        assertThat(productRepository.existsByNameIgnoreCaseAndIdNot("Wireless Mouse", mouse.getId() + 1000)).isTrue();
    }

    @Test
    @DisplayName("findDistinctCategories: returns each category once, sorted")
    void distinctCategories() {
        List<String> categories = productRepository.findDistinctCategories();

        assertThat(categories).containsExactly("Books", "Electronics", "Furniture");
    }

    @Test
    @DisplayName("countByStockLessThan: counts low stock products")
    void lowStockCount() {
        assertThat(productRepository.countByStockLessThan(10)).isEqualTo(2); // keyboard (0) and chair (4)
    }

    @Test
    @DisplayName("totalInventoryValue: sum of price x stock")
    void inventoryValue() {
        assertThat(productRepository.totalInventoryValue()).isEqualByComparingTo("74936.00");
    }

    @Test
    @DisplayName("unique constraint: a second product with the same name is rejected by the database")
    void duplicateNameRejectedByDatabase() {
        assertThatThrownBy(() -> productRepository.saveAndFlush(product("Wireless Mouse", "Electronics", "1.00", 1)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
