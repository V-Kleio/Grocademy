package com.grocademy.dto;

import java.math.BigDecimal;
import java.util.List;

public record CourseResponseDto(
    String id,
    String title,
    String description,
    String instructor,
    List<String> topics,
    BigDecimal price,
    String thumbnailImage,
    Integer totalModules,
    String createdAt,
    String updatedAt
) {}