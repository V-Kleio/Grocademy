package com.grocademy.dto;

public record ModuleDto(
    Long id,
    String title,
    int moduleOrder,
    boolean isCompleted
) {}
