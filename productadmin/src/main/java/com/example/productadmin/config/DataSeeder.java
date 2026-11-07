
package com.example.productadmin.config;

import com.example.productadmin.entity.Product;
import com.example.productadmin.entity.Role;
import com.example.productadmin.entity.User;
import com.example.productadmin.repository.ProductRepository;
import com.example.productadmin.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@ConditionalOnProperty(
        name = "app.seed.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log =
            LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminPassword;
    private final String userPassword;

    public DataSeeder(
            UserRepository userRepository,
            ProductRepository productRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.admin-password:Admin@12345}") String adminPassword,
            @Value("${app.seed.user-password:User@12345}") String userPassword
    ) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
        this.userPassword = userPassword;
    }

    @Override
    public void run(String... args) {

        User admin = userRepository
                .findByUsernameIgnoreCase("admin")
                .orElseGet(() -> new User(
                        "admin",
                        "admin@example.com",
                        passwordEncoder.encode(adminPassword),
                        Role.ADMIN
                ));

        admin.setPassword(passwordEncoder.encode(adminPassword));
        userRepository.save(admin);

        log.info("ADMIN account password updated");

        User user = userRepository
                .findByUsernameIgnoreCase("user")
                .orElseGet(() -> new User(
                        "user",
                        "user@example.com",
                        passwordEncoder.encode(userPassword),
                        Role.USER
                ));

        user.setPassword(passwordEncoder.encode(userPassword));
        userRepository.save(user);

        log.info("USER account password updated");

        if (productRepository.count() == 0) {
            productRepository.saveAll(sampleProducts());
            log.info("Seeded sample products");
        }
    }

    private List<Product> sampleProducts() {
        return List.of(
                p("Wireless Mouse", "Ergonomic 2.4GHz wireless mouse with silent clicks", "Electronics", "799.00", 120, 4.4),
                p("Mechanical Keyboard", "Hot-swappable mechanical keyboard with blue switches", "Electronics", "3499.00", 45, 4.6),
                p("27-inch Monitor", "Full HD IPS monitor with 75Hz refresh rate", "Electronics", "12999.00", 18, 4.5),
                p("Bluetooth Speaker", "Portable waterproof speaker, 12 hour battery", "Electronics", "1999.00", 8, 4.2),
                p("USB-C Hub", "7-in-1 hub with HDMI, USB 3.0 and card reader", "Accessories", "1499.00", 60, 4.1),
                p("Laptop Stand", "Adjustable aluminium laptop stand", "Accessories", "999.00", 75, 4.3),
                p("Phone Case", "Shock-proof transparent case", "Accessories", "299.00", 0, 3.9),
                p("Webcam 1080p", "Full HD webcam with built-in microphone", "Accessories", "2299.00", 33, 4.0),
                p("Office Chair", "Ergonomic mesh chair with lumbar support", "Furniture", "7999.00", 12, 4.5),
                p("Standing Desk", "Height-adjustable electric standing desk", "Furniture", "18999.00", 6, 4.7),
                p("Bookshelf", "5-tier wooden bookshelf", "Furniture", "4599.00", 20, 4.2),
                p("Clean Code", "A handbook of agile software craftsmanship", "Books", "699.00", 55, 4.8),
                p("Effective Java", "Best practices for the Java platform", "Books", "899.00", 40, 4.9),
                p("Spring in Action", "Hands-on guide to the Spring framework", "Books", "999.00", 25, 4.6),
                p("Air Fryer", "4.2 litre digital air fryer", "Home Appliances", "5499.00", 14, 4.3),
                p("Electric Kettle", "1.5 litre stainless steel kettle", "Home Appliances", "899.00", 9, 4.1)
        );
    }

    private Product p(
            String name,
            String description,
            String category,
            String price,
            int stock,
            double rating
    ) {
        return new Product(
                name,
                description,
                category,
                new BigDecimal(price),
                stock,
                rating
        );
    }
}

