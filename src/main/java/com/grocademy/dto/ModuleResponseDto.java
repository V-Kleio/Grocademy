package com.grocademy.dto;

public record ModuleResponseDto(
    String id,
    String courseId,
    String title,
    String description,
    Integer order,
    String pdfContent,
    String videoContent,
    Boolean isCompleted,
    String createdAt,
    String updatedAt
) {}
