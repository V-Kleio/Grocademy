package com.grocademy.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.grocademy.entity.Course;

public record CourseDto(
    Long id,
    String title,
    String description,
    String instructor,
    List<String> topics,
    BigDecimal price,
    String thumbnailImage,
    Boolean isPurchased,
    Boolean isCompleted,
    Integer totalModules,
    Integer progressPercentage,
    Instant createdAt,
    Instant updatedAt
) {
    public static CourseDto fromEntity(Course course) {
        return new CourseDto(
            course.getId(),
            course.getTitle(),
            course.getDescription(),
            course.getInstructor(),
            course.getTopics(),
            course.getPrice(),
            course.getThumbnailImageUrl(),
            false, false, 0, 0,
            course.getCreatedAt(),
            course.getUpdatedAt()
        );
    }

    public static CourseDto fromEntityWithUserData(com.grocademy.entity.Course course, 
                                                   boolean isPurchased,
                                                   boolean isCompleted,
                                                   int totalModules,
                                                   int progressPercentage) {
        return new CourseDto(
            course.getId(),
            course.getTitle(),
            course.getDescription(),
            course.getInstructor(),
            course.getTopics(),
            course.getPrice(),
            course.getThumbnailImageUrl(),
            isPurchased,
            isCompleted,
            totalModules,
            progressPercentage,
            course.getCreatedAt(),
            course.getUpdatedAt()
        );
    }

    public CourseResponseDto toApiResponse() {
        return new CourseResponseDto(
            id != null ? id.toString() : null,
            title,
            description,
            instructor,
            topics,
            price,
            thumbnailImage,
            totalModules,
            createdAt != null ? createdAt.toString() : null,
            updatedAt != null ? updatedAt.toString() : null
        );
    }
}
