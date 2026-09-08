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
import com.uade.e_commerce.model.ProductImage;
import com.uade.e_commerce.model.ProductType;
import com.uade.e_commerce.model.User;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
class ProductImageRepositoryTest {

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private Product product;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.save(new Category(null, "Tecnología", null));
        User publisher = userRepository.save(
                new User(null, "Ada", "Lovelace", "ada@test.com", "hash", "L1", LocalDateTime.now(), true));

        product = new Product();
        product.setName("Notebook");
        product.setPrice(100.0);
        product.setType(ProductType.PHYSICAL);
        product.setCategory(category);
        product.setPublisher(publisher);
        product = productRepository.save(product);
    }

    private ProductImage buildImage(String url, int order) {
        ProductImage image = new ProductImage();
        image.setUrl(url);
        image.setImageOrder(order);
        image.setProduct(product);
        return image;
    }

    @Test
    void findByProductIdOrderByImageOrderAsc_returnsImagesInOrder() {
        productImageRepository.save(buildImage("http://img/2.png", 2));
        productImageRepository.save(buildImage("http://img/1.png", 1));

        var result = productImageRepository.findByProductIdOrderByImageOrderAsc(product.getId());

        assertThat(result).extracting(ProductImage::getUrl)
                .containsExactly("http://img/1.png", "http://img/2.png");
    }
}
