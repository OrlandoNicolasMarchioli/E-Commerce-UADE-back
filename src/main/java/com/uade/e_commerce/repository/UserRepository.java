package com.uade.e_commerce.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.e_commerce.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
