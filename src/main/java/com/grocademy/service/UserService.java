package com.grocademy.service;

import com.grocademy.dto.UserDto;
import com.grocademy.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface UserService {
    User registerUser(String firstName, String lastName, String username, String email, String password);

    Page<UserDto> getAllUsers(String query, Pageable pageable);
    UserDto getUserById(Long id);
    UserDto updateUser(Long id, String email, String username, String firstName, String lastName, String password);
    UserDto updateUserBalance(Long id, BigDecimal increment);
    void deleteUser(Long id);

    int getUserCoursesPurchased(Long userId);
    UserDto getCurrentUserInfo(String username);
}
