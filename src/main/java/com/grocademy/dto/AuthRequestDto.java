package com.grocademy.dto;

public record AuthRequestDto(
    String identifier,
    String password
) {}
