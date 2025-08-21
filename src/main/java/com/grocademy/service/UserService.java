package com.grocademy.service;

import com.grocademy.entity.User;

public interface UserService {
    User registerUser(String firstName, String lastName, String username, String email, String password);
}
