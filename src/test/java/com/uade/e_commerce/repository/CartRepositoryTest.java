package com.uade.e_commerce.repository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.transaction.annotation.Transactional;

import com.uade.e_commerce.model.Cart;
import com.uade.e_commerce.model.User;

@SpringBootTest(
    webEnvironment = WebEnvironment.NONE
)
@Transactional
class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {

        user = userRepository.save(
            new User(
                null,
                "Franco",
                "Parodi",
                "franco@test.com",
                "hash",
                "1202784",
                LocalDateTime.now(),
                true
            )
        );
    }

    @Test
    void findByUserId_cartExists_returnsCart() {

        Cart cart = new Cart();
        cart.setUser(user);

        cart = cartRepository.save(cart);

        var result =
            cartRepository.findByUserId(
                user.getId()
            );

        assertThat(result).isPresent();

        assertThat(
            result.get().getId()
        ).isEqualTo(cart.getId());

        assertThat(
            result.get().getUser().getId()
        ).isEqualTo(user.getId());
    }

    @Test
    void findByUserId_cartDoesNotExist_returnsEmpty() {

        var result =
            cartRepository.findByUserId(
                user.getId()
            );

        assertThat(result).isEmpty();
    }
}