package com.uade.e_commerce.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.e_commerce.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data builds the query on its own from the method name, no need
    // to write the SQL or annotate with @Query.

    // Returns Optional instead of User to make it explicit that the email
    // might not exist, so there's no need to go around checking for null.
    Optional<User> findByEmail(String email);

    // Cheaper than fetching the whole user when we only want to know
    // whether the email is already taken.
    boolean existsByEmail(String email);
}
