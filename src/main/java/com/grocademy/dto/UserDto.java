package com.grocademy.dto;

import java.math.BigDecimal;
import java.time.Instant;
import com.grocademy.entity.User;

public record UserDto(
    Long id,
    String firstName,
    String lastName,
    String username,
    String email,
    BigDecimal balance,
    String role,
    Integer coursesPurchased,
    Instant createdAt,
    Instant updatedAt
) {
    public static UserDto fromEntity(User user) {
        return new UserDto(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getUsername(),
            user.getEmail(),
            user.getBalance(),
            user.getRole().name(),
            null,
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }

    public static UserDto fromEntityWithDetails(User user, int coursesPurchased) {
        return new UserDto(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getUsername(),
            user.getEmail(),
            user.getBalance(),
            user.getRole().name(),
            coursesPurchased,
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }

    public UserResponseDto toApiResponse() {
        return new UserResponseDto(
            id != null ? id.toString() : null,
            username,
            email,
            firstName,
            lastName,
            balance,
            coursesPurchased
        );
    }
}