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
import com.uade.e_commerce.model.CartItem;
import com.uade.e_commerce.model.Category;
import com.uade.e_commerce.model.Product;
import com.uade.e_commerce.model.ProductType;
import com.uade.e_commerce.model.User;

@SpringBootTest(
    webEnvironment = WebEnvironment.NONE
)
@Transactional
class CartItemRepositoryTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private Cart cart;
    private Product product;

    @BeforeEach
    void setUp() {

        Category category =
            categoryRepository.save(
                new Category(
                    null,
                    "Librería",
                    "Útiles escolares"
                )
            );

        User user =
            userRepository.save(
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

        product = new Product();
        product.setName("Cuaderno");
        product.setDescription(
            "Cuaderno universitario"
        );
        product.setPrice(1000.0);
        product.setType(ProductType.PHYSICAL);
        product.setStock(10);
        product.setCategory(category);
        product.setPublisher(user);

        product =
            productRepository.save(product);

        cart = new Cart();
        cart.setUser(user);

        cart = cartRepository.save(cart);
    }

    private CartItem buildItem(
        Integer quantity
    ) {

        CartItem item = new CartItem();

        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(quantity);

        return item;
    }

    @Test
    void findByCartIdAndProductId_itemExists_returnsItem() {

        CartItem item =
            cartItemRepository.save(
                buildItem(2)
            );

        var result =
            cartItemRepository
                .findByCartIdAndProductId(
                    cart.getId(),
                    product.getId()
                );

        assertThat(result).isPresent();

        assertThat(
            result.get().getId()
        ).isEqualTo(item.getId());

        assertThat(
            result.get().getQuantity()
        ).isEqualTo(2);
    }

    @Test
    void findByCartIdAndProductId_itemDoesNotExist_returnsEmpty() {

        var result =
            cartItemRepository
                .findByCartIdAndProductId(
                    cart.getId(),
                    product.getId()
                );

        assertThat(result).isEmpty();
    }

    @Test
    void findByCartIdOrderByIdAsc_returnsItems() {

        cartItemRepository.save(
            buildItem(3)
        );

        var result =
            cartItemRepository
                .findByCartIdOrderByIdAsc(
                    cart.getId()
                );

        assertThat(result).hasSize(1);

        assertThat(
            result.get(0).getQuantity()
        ).isEqualTo(3);
    }

    @Test
    void deleteByCartId_removesAllCartItems() {

        cartItemRepository.save(
            buildItem(2)
        );

        cartItemRepository.deleteByCartId(
            cart.getId()
        );

        cartItemRepository.flush();

        var result =
            cartItemRepository
                .findByCartIdOrderByIdAsc(
                    cart.getId()
                );

        assertThat(result).isEmpty();
    }
}