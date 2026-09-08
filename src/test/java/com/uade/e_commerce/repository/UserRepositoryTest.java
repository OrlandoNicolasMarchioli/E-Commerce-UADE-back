package com.uade.e_commerce.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.transaction.annotation.Transactional;

import com.uade.e_commerce.model.User;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User persistUser(String email) {
        return userRepository.save(new User(null, "Ada", "Lovelace", email, "hash", "L1", LocalDateTime.now(), true));
    }

    @Test
    void findByEmail_existingEmail_returnsUser() {
        persistUser("ada@test.com");

        Optional<User> found = userRepository.findByEmail("ada@test.com");

        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Ada");
    }

    @Test
    void findByEmail_unknownEmail_returnsEmpty() {
        assertThat(userRepository.findByEmail("nobody@test.com")).isEmpty();
    }

    @Test
    void existsByEmail_afterSave_isTrue() {
        persistUser("ada@test.com");

        assertThat(userRepository.existsByEmail("ada@test.com")).isTrue();
    }

    @Test
    void existsByEmail_unknownEmail_isFalse() {
        assertThat(userRepository.existsByEmail("nobody@test.com")).isFalse();
    }
}
