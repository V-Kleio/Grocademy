package com.grocademy.dto;

import java.math.BigDecimal;

public record UserBalanceUpdateDto(
    BigDecimal increment
) {}
