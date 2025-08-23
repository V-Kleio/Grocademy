package com.grocademy.dto;

import java.time.Instant;
import com.grocademy.entity.Module;

public record ModuleDto(
    Long id,
    Long courseId,
    String title,
    String description,
    String pdfContent,
    String videoContent,
    Integer moduleOrder,
    Boolean isCompleted,
    Instant createdAt,
    Instant updatedAt
) {
    public static ModuleDto fromEntity(Module module) {
        return new ModuleDto(
            module.getId(),
            module.getCourse().getId(),
            module.getTitle(),
            module.getDescription(),
            module.getPdfContentUrl(),
            module.getVideoContentUrl(),
            module.getModuleOrder(),
            false,
            module.getCreatedAt(),
            module.getUpdatedAt()
        );
    }

    public static ModuleDto fromEntityWithUserData(Module module, boolean isCompleted) {
        return new ModuleDto(
            module.getId(),
            module.getCourse().getId(),
            module.getTitle(),
            module.getDescription(),
            module.getPdfContentUrl(),
            module.getVideoContentUrl(),
            module.getModuleOrder(),
            isCompleted,
            module.getCreatedAt(),
            module.getUpdatedAt()
        );
    }

    public ModuleResponseDto toApiResponse() {
        return new ModuleResponseDto(
            id != null ? id.toString() : null,
            courseId != null ? courseId.toString() : null,
            title,
            description,
            moduleOrder,
            pdfContent,
            videoContent,
            Boolean.TRUE.equals(isCompleted),
            createdAt != null ? createdAt.toString() : null,
            updatedAt != null ? updatedAt.toString() : null
        );
    }
}