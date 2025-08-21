package com.grocademy.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.grocademy.entity.User;
import com.grocademy.repository.UserRepository;
import com.grocademy.service.UserService;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(String firstName, String lastName, String username, String email, String password) {
        if (userRepository.findByUsername(username).isPresent() || userRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("Username or Email already exists");
        }

        User user = new User.Builder()
            .firstName(firstName)
            .lastName(lastName)
            .username(username)
            .email(email)
            .passwordHash(passwordEncoder.encode(password))
            .build();

        return userRepository.save(user);
    }
}

