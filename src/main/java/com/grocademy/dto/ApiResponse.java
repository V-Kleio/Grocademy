package com.grocademy.dto;

public record ApiResponse<T>(
    String status,
    String message,
    T data
) {}
