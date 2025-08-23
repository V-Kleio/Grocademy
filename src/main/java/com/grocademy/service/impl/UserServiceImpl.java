package com.grocademy.service.impl;

import com.grocademy.dto.UserDto;
import com.grocademy.entity.User;
import com.grocademy.repository.PurchasedCourseRepository;
import com.grocademy.repository.UserRepository;
import com.grocademy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PurchasedCourseRepository purchasedCourseRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, 
                          PurchasedCourseRepository purchasedCourseRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.purchasedCourseRepository = purchasedCourseRepository;
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

    @Override
    public Page<UserDto> getAllUsers(String query, Pageable pageable) {
        Page<User> userPage;

        if (query == null || query.isEmpty()) {
            userPage = userRepository.findAll(pageable);
        } else {
            userPage = userRepository.findByUsernameContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                query, query, query, query, pageable);
        }

        return userPage.map(UserDto::fromEntity);
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User not found, ID: " + id));

        int coursesPurchased = getUserCoursesPurchased(id);
        return UserDto.fromEntityWithDetails(user, coursesPurchased);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, String email, String username, String firstName, String lastName, String password) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User not found, ID: " + id));

        if (user.isAdmin()) {
            throw new SecurityException("Admin users cannot be modified");
        }

        if (!user.getUsername().equals(username) && userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (!user.getEmail().equals(email) && userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        user.setEmail(email);
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);

        if (password != null && !password.isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(password));
        }

        User savedUser = userRepository.save(user);
        return UserDto.fromEntity(savedUser);
    }

    @Override
    @Transactional
    public UserDto updateUserBalance(Long id, BigDecimal increment) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User not found, ID: " + id));

        BigDecimal newBalance = user.getBalance().add(increment);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }

        user.setBalance(newBalance);
        User savedUser = userRepository.save(user);
        return UserDto.fromEntity(savedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User not found, ID: " + id));

        if (user.isAdmin()) {
            throw new SecurityException("Admin users cannot be deleted");
        }

        userRepository.deleteById(id);
    }

    @Override
    public int getUserCoursesPurchased(Long userId) {
        return purchasedCourseRepository.countByUserId(userId);
    }

    @Override
    public UserDto getCurrentUserInfo(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
        return UserDto.fromEntity(user);
    }
}
