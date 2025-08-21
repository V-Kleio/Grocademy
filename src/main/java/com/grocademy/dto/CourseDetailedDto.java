package com.grocademy.dto;

import java.math.BigDecimal;
import java.util.List;

public record CourseDetailedDto(
    Long id,
    String title,
    String description,
    String instructor,
    List<String> topics,
    BigDecimal price,
    String thumbnailImageUrl,
    boolean isPurchased,
    boolean isCompleted
) {}
