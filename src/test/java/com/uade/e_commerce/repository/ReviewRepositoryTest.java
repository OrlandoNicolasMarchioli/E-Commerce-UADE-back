package com.uade.e_commerce.repository;

import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import com.uade.e_commerce.model.Category;
import com.uade.e_commerce.model.Product;
import com.uade.e_commerce.model.ProductType;
import com.uade.e_commerce.model.Review;
import com.uade.e_commerce.model.User;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private Product product;
    private User reviewer;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.save(new Category(null, "Tecnología", null));
        User publisher = userRepository.save(
                new User(null, "Ada", "Lovelace", "ada@test.com", "hash", "L1", LocalDateTime.now(), true));
        reviewer = userRepository.save(
                new User(null, "Grace", "Hopper", "grace@test.com", "hash", "L2", LocalDateTime.now(), true));

        product = new Product();
        product.setName("Notebook");
        product.setPrice(new BigDecimal("100.00"));
        product.setType(ProductType.PHYSICAL);
        product.setCategory(category);
        product.setPublisher(publisher);
        product = productRepository.save(product);
    }

    private Review buildReview(User user) {
        Review review = new Review();
        review.setRating(5);
        review.setComment("Muy bueno");
        review.setDate(LocalDateTime.now());
        review.setProduct(product);
        review.setUser(user);
        return review;
    }

    @Test
    void findByProductIdOrderByDateDesc_returnsMostRecentFirst() {
        Review older = buildReview(reviewer);
        older.setDate(LocalDateTime.now().minusDays(1));
        reviewRepository.save(older);

        User anotherReviewer = userRepository.save(
                new User(null, "Linus", "Torvalds", "linus@test.com", "hash", "L3", LocalDateTime.now(), true));
        Review newer = buildReview(anotherReviewer);
        newer.setDate(LocalDateTime.now());
        reviewRepository.save(newer);

        var result = reviewRepository.findByProductIdOrderByDateDesc(product.getId());

        assertThat(result).extracting(Review::getUser).containsExactly(anotherReviewer, reviewer);
    }

    @Test
    void existsByProductIdAndUserId_afterSave_isTrue() {
        reviewRepository.save(buildReview(reviewer));

        assertThat(reviewRepository.existsByProductIdAndUserId(product.getId(), reviewer.getId())).isTrue();
    }

    @Test
    void existsByProductIdAndUserId_noReview_isFalse() {
        assertThat(reviewRepository.existsByProductIdAndUserId(product.getId(), reviewer.getId())).isFalse();
    }

    @Test
    void save_secondReviewFromSameUserForSameProduct_violatesUniqueConstraint() {
        reviewRepository.save(buildReview(reviewer));
        reviewRepository.flush();

        assertThatThrownBy(() -> {
            reviewRepository.save(buildReview(reviewer));
            reviewRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
