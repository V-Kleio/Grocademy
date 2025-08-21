package com.grocademy.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grocademy.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
}