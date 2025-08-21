package com.grocademy.dto;

import java.math.BigDecimal;

public record CourseDto(
    Long id,
    String title,
    String instructor,
    BigDecimal price,
    String thumbnailImageUrl,
    boolean isPurchased
) {}
