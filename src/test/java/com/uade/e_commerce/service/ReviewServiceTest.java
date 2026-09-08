package com.uade.e_commerce.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uade.e_commerce.dto.review.ReviewRequestDTO;
import com.uade.e_commerce.exception.DuplicateReviewException;
import com.uade.e_commerce.exception.ProductNotFoundException;
import com.uade.e_commerce.exception.ReviewNotFoundException;
import com.uade.e_commerce.exception.UserNotFoundException;
import com.uade.e_commerce.model.Product;
import com.uade.e_commerce.model.Review;
import com.uade.e_commerce.model.User;
import com.uade.e_commerce.repository.ProductRepository;
import com.uade.e_commerce.repository.ReviewRepository;
import com.uade.e_commerce.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Product product(Long id) {
        Product product = new Product();
        product.setId(id);
        return product;
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private Review review(Long id, Product product, User user) {
        Review review = new Review();
        review.setId(id);
        review.setRating(5);
        review.setComment("Muy bueno");
        review.setProduct(product);
        review.setUser(user);
        return review;
    }

    @Test
    void getReviewsByProduct_productExists_returnsOrderedList() {
        when(productRepository.existsById(1L)).thenReturn(true);
        Review review = review(10L, product(1L), user(2L));
        when(reviewRepository.findByProductIdOrderByDateDesc(1L)).thenReturn(List.of(review));

        List<Review> result = reviewService.getReviewsByProduct(1L);

        assertThat(result).containsExactly(review);
    }

    @Test
    void getReviewsByProduct_productNotFound_throws() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.getReviewsByProduct(99L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void getReviewById_belongsToProduct_returnsReview() {
        Review review = review(10L, product(1L), user(2L));
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));

        assertThat(reviewService.getReviewById(1L, 10L)).isEqualTo(review);
    }

    @Test
    void getReviewById_doesNotExist_throwsNotFound() {
        when(reviewRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getReviewById(1L, 10L))
                .isInstanceOf(ReviewNotFoundException.class);
    }

    @Test
    void getReviewById_belongsToDifferentProduct_throwsNotFound() {
        Review review = review(10L, product(2L), user(2L));
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.getReviewById(1L, 10L))
                .isInstanceOf(ReviewNotFoundException.class);
    }

    @Test
    void createReview_valid_savesReview() {
        Product product = product(1L);
        User user = user(2L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(reviewRepository.existsByProductIdAndUserId(1L, 2L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewRequestDTO dto = new ReviewRequestDTO();
        dto.setRating(4);
        dto.setComment("Bastante bueno");

        Review result = reviewService.createReview(1L, 2L, dto);

        assertThat(result.getRating()).isEqualTo(4);
        assertThat(result.getComment()).isEqualTo("Bastante bueno");
        assertThat(result.getProduct()).isEqualTo(product);
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getDate()).isNotNull();
    }

    @Test
    void createReview_productNotFound_throwsAndDoesNotCheckDuplicate() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(99L, 2L, new ReviewRequestDTO()))
                .isInstanceOf(ProductNotFoundException.class);

        verify(reviewRepository, never()).existsByProductIdAndUserId(any(), any());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_userNotFound_throws() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product(1L)));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(1L, 99L, new ReviewRequestDTO()))
                .isInstanceOf(UserNotFoundException.class);

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_userAlreadyReviewedProduct_throwsDuplicateAndDoesNotSave() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product(1L)));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L)));
        when(reviewRepository.existsByProductIdAndUserId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(1L, 2L, new ReviewRequestDTO()))
                .isInstanceOf(DuplicateReviewException.class);

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void updateReview_belongsToProduct_updatesRatingAndComment() {
        Review existing = review(10L, product(1L), user(2L));
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewRequestDTO dto = new ReviewRequestDTO();
        dto.setRating(1);
        dto.setComment("Cambié de opinión");

        Review result = reviewService.updateReview(1L, 10L, dto);

        assertThat(result.getRating()).isEqualTo(1);
        assertThat(result.getComment()).isEqualTo("Cambié de opinión");
    }

    @Test
    void updateReview_notFound_throwsAndDoesNotSave() {
        when(reviewRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.updateReview(1L, 10L, new ReviewRequestDTO()))
                .isInstanceOf(ReviewNotFoundException.class);

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void deleteReview_belongsToProduct_deletes() {
        Review existing = review(10L, product(1L), user(2L));
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(existing));

        reviewService.deleteReview(1L, 10L);

        verify(reviewRepository).delete(existing);
    }

    @Test
    void deleteReview_belongsToDifferentProduct_throwsAndDoesNotDelete() {
        Review existing = review(10L, product(2L), user(2L));
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> reviewService.deleteReview(1L, 10L))
                .isInstanceOf(ReviewNotFoundException.class);

        verify(reviewRepository, never()).delete(any());
    }
}
