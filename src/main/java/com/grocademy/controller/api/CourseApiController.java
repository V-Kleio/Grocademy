package com.grocademy.controller.api;

import com.grocademy.dto.ApiResponse;
import com.grocademy.dto.CourseDto;
import com.grocademy.dto.CourseResponseDto;
import com.grocademy.entity.User;
import com.grocademy.service.CourseService;
import com.grocademy.service.FileStorageService;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.grocademy.repository.UserRepository;

@RestController
@RequestMapping("/api/courses")
public class CourseApiController {
    private final CourseService courseService;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    @Autowired
    public CourseApiController(
        CourseService courseService,
        FileStorageService fileStorageService,
        UserRepository userRepository
    ) {
        this.courseService = courseService;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @SuppressWarnings("UseSpecificCatch")
    public ResponseEntity<ApiResponse<Object>> createCourse(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String instructor,
            @RequestParam String[] topics,
            @RequestParam Double price,
            @RequestParam(required = false) MultipartFile thumbnail_image,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (!isAdmin(userDetails)) {
                return ResponseEntity.status(403).body(
                    new ApiResponse<>("error", "Admin access required", null)
                );
            }

            String thumbnailUrl = null;
            if (thumbnail_image != null && !thumbnail_image.isEmpty()) {
                thumbnailUrl = fileStorageService.storeFile(thumbnail_image, "thumbnails");
            }

            CourseDto courseDto = courseService.createCourse(
                title,
                description,
                instructor,
                Arrays.asList(topics),
                BigDecimal.valueOf(price),
                thumbnailUrl
            );

            return ResponseEntity.ok(new ApiResponse<>("success", "Course created successfully", courseDto.toApiResponse()));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(
                new ApiResponse<>("error", e.getMessage(), null)
            );
        }
    }

    @PostMapping("/{id}/buy")
    public ResponseEntity<ApiResponse<Object>> buyCourse(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userDetails.getUsername()));

            courseService.buyCourse(id, user.getId());

            Map<String, Object> responseData = Map.of(
                "course_id", id.toString(),
                "user_id", user.getId().toString(),
                "message", "Course purchased successfully"
            );

            return ResponseEntity.ok(new ApiResponse<>("success", "Course purchased Successfully", responseData));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(
                new ApiResponse<>("error", "Course not found", null)
            );
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(
                new ApiResponse<>("error", e.getMessage(), null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ApiResponse<>("error", "Internal server error", null)
            );
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getAllCourses(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (page < 1) page = 1;
            if (limit < 1 || limit > 50) limit = 15;

            Pageable pageable = PageRequest.of(page - 1, limit);
            Page<CourseDto> coursePage = courseService.apiGetAllCourses(q, pageable);

            List<CourseResponseDto> courseData = coursePage.getContent().stream()
                .map(CourseDto::toApiResponse)
                .toList();

            Map<String, Object> response = Map.of(
                "data", courseData,
                "pagination", Map.of(
                    "current_page", page,
                    "total_pages", coursePage.getTotalPages(),
                    "total_items", coursePage.getTotalElements()
                )
            );

            return ResponseEntity.ok(new ApiResponse<>("success", "Courses retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ApiResponse<>("error", "Internal server error", null)
            );
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> getCourseById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            CourseDto courseDto = courseService.apiGetCourseById(id);

            return ResponseEntity.ok(new ApiResponse<>("success", "Course retrieved successfully", courseDto.toApiResponse()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(
                new ApiResponse<>("error", "Course not found", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ApiResponse<>("error", "Internal server error", null)
            );
        }
    }

    @GetMapping("/my-courses")
    public ResponseEntity<ApiResponse<Object>> getMyCourse(
        @RequestParam(defaultValue = "") String q,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "15") int limit,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            if (page < 1) page = 1;
            if (limit < 1 || limit > 50) limit = 15;

            User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userDetails.getUsername()));

            Pageable pageable = PageRequest.of(page - 1, limit);
            Page<CourseDto> coursePage = courseService.findPurchasedCourses(user.getId(), q, pageable);

            List<CourseResponseDto> courseData = coursePage.getContent().stream()
                .map(CourseDto::toApiResponse)
                .toList();

            Map<String, Object> response = Map.of(
                "data", courseData,
                "pagination", Map.of(
                    "current_page", page,
                    "total_page", coursePage.getTotalPages(),
                    "total_items", coursePage.getTotalElements()
                )
            );

            return ResponseEntity.ok(new ApiResponse<>("success", "My course retrieved successfully", response));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(
                new ApiResponse<>("error", "User not found", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ApiResponse<>("error", "Internal server error", null)
            );
        }
    }

    @PutMapping("/{id}")
    @SuppressWarnings("UseSpecificCatch")
    public ResponseEntity<ApiResponse<Object>> updateCourse(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String instructor,
            @RequestParam String[] topics,
            @RequestParam Double price,
            @RequestParam(required = false) MultipartFile thumbnail_image,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (!isAdmin(userDetails)) {
                return ResponseEntity.status(403).body(
                    new ApiResponse<>("error", "Admin access required", null)
                );
            }

            CourseDto existingCourse = courseService.apiGetCourseById(id);

            String thumbnailUrl = existingCourse.thumbnailImage();
            if (thumbnail_image != null && !thumbnail_image.isEmpty()) {
                if (thumbnailUrl != null) {
                    fileStorageService.deleteFile(thumbnailUrl);
                }
                thumbnailUrl = fileStorageService.storeFile(thumbnail_image, "thumbnails");
            }

            CourseDto updatedCourse = courseService.updateCourse(
                id, title, description, instructor, 
                Arrays.asList(topics), BigDecimal.valueOf(price), thumbnailUrl
            );

            return ResponseEntity.ok(new ApiResponse<>("success", "Course updated successfully", updatedCourse.toApiResponse()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(
                new ApiResponse<>("error", "Course not found", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(400).body(
                new ApiResponse<>("error", e.getMessage(), null)
            );
        }
    }
    
    @DeleteMapping("/{id}")
    @SuppressWarnings("UseSpecificCatch")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (!isAdmin(userDetails)) {
                return ResponseEntity.status(403).build();
            }

            CourseDto course = courseService.apiGetCourseById(id);

            if (course.thumbnailImage() != null) {
                fileStorageService.deleteFile(course.thumbnailImage());
            }

            courseService.deleteCourse(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    private boolean isAdmin(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}
