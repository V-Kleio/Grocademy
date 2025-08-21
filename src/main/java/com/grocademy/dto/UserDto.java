package com.grocademy.dto;

public record UserDto(
    String firstName,
    String lastName,
    String username,
    String email,
    String password
) {}
