package com.grocademy.dto;

import java.math.BigDecimal;

public record UserResponseDto(
    String id,
    String username,
    String email,
    String firstName,
    String lastName,
    BigDecimal balance,
    Integer coursesPurchased
) {}
