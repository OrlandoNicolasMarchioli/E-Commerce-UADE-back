package com.uade.e_commerce.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.e_commerce.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data arma la query sola a partir del nombre del método, no hace
    // falta escribir el SQL ni anotar con @Query.

    // Devuelve Optional en vez de User para que quede explícito que el email
    // puede no existir y no haya que andar chequeando null.
    Optional<User> findByEmail(String email);

    // Más barato que traer el usuario entero cuando solo queremos saber si el
    // email ya está tomado.
    boolean existsByEmail(String email);
}
