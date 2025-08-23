package com.grocademy.dto;

public record UserUpdateDto(
    String email,
    String username,
    String firstName,
    String lastName,
    String password
) {
    public UserUpdateDto() {
        this("", "", "", "", "");
    }
}
