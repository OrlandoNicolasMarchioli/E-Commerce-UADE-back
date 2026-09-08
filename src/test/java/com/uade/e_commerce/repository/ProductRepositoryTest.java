package com.uade.e_commerce.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.transaction.annotation.Transactional;

import com.uade.e_commerce.model.Category;
import com.uade.e_commerce.model.Product;
import com.uade.e_commerce.model.ProductType;
import com.uade.e_commerce.model.User;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private Category tech;
    private Category books;
    private User publisher;

    @BeforeEach
    void setUp() {
        tech = categoryRepository.save(new Category(null, "Tecnología", null));
        books = categoryRepository.save(new Category(null, "Libros", null));
        publisher = userRepository.save(
                new User(null, "Ada", "Lovelace", "ada@test.com", "hash", "L1", LocalDateTime.now(), true));
    }

    private Product buildProduct(String name, Category category) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(100.0);
        product.setType(ProductType.PHYSICAL);
        product.setCategory(category);
        product.setPublisher(publisher);
        return product;
    }

    @Test
    void findAllByOrderByNameAsc_returnsProductsAlphabetically() {
        productRepository.save(buildProduct("Zapatilla", tech));
        productRepository.save(buildProduct("Auricular", tech));

        var result = productRepository.findAllByOrderByNameAsc();

        assertThat(result).extracting(Product::getName).containsExactly("Auricular", "Zapatilla");
    }

    @Test
    void findByCategoryId_returnsOnlyMatchingProducts() {
        productRepository.save(buildProduct("Notebook", tech));
        productRepository.save(buildProduct("Novela", books));

        var result = productRepository.findByCategoryId(tech.getId());

        assertThat(result).extracting(Product::getName).containsExactly("Notebook");
    }
}
