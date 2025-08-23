package com.grocademy.controller.api;

import com.grocademy.dto.ApiResponse;
import com.grocademy.dto.ModuleDto;
import com.grocademy.service.FileStorageService;
import com.grocademy.service.ModuleService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Map;

import com.grocademy.dto.ModuleResponseDto;

@RestController
@RequestMapping("/api")
public class ModuleApiController {
    private final ModuleService moduleService;
    private final FileStorageService fileStorageService;

    @Autowired
    public ModuleApiController(ModuleService moduleService, FileStorageService fileStorageService) {
        this.moduleService = moduleService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/courses/{courseId}/modules")
    @SuppressWarnings("UseSpecificCatch")
    public ResponseEntity<ApiResponse<Object>> createModule(
            @PathVariable Long courseId,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam(required = false) MultipartFile pdf_content,
            @RequestParam(required = false) MultipartFile video_content,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (!isAdmin(userDetails)) {
                return ResponseEntity.status(403).body(
                    new ApiResponse<>("error", "Admin access required", null)
                );
            }

            String pdfUrl = null;
            String videoUrl = null;

            if (pdf_content != null && !pdf_content.isEmpty()) {
                pdfUrl = fileStorageService.storeFile(pdf_content, "pdfs");
            }

            if (video_content != null && !video_content.isEmpty()) {
                videoUrl = fileStorageService.storeFile(video_content, "videos");
            }

            ModuleDto moduleDto = moduleService.createModule(courseId, title, description, pdfUrl, videoUrl);

            return ResponseEntity.ok(new ApiResponse<>("success", "Module created successfully", moduleDto.toApiResponse()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(
                new ApiResponse<>("error", "Course not found", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(400).body(
                new ApiResponse<>("error", e.getMessage(), null)
            );
        }
    }

    @GetMapping("/courses/{courseId}/modules")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getModulesByCourse(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (page < 1) page = 1;
            if (limit < 1 || limit > 50) limit = 15;

            Pageable pageable = PageRequest.of(page - 1, limit);

            Page<ModuleDto> modulePage;
            if (isAdmin(userDetails)) {
                modulePage = moduleService.getModulesByCourse(courseId, pageable);
            } else {
                String username = userDetails.getUsername();
                modulePage = moduleService.userGetModulesByCourse(courseId, username, pageable);
            }

            List<ModuleResponseDto> moduleData = modulePage.getContent().stream()
                .map(ModuleDto::toApiResponse)
                .toList();

            Map<String, Object> response = Map.of(
                "data", moduleData,
                "pagination", Map.of(
                    "current_page", page,
                    "total_pages", modulePage.getTotalPages(),
                    "total_items", modulePage.getTotalElements()
                )
            );

            return ResponseEntity.ok(new ApiResponse<>("success", "Modules retrieved successfully", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(
                new ApiResponse<>("error", e.getMessage(), null)
            );
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

    @GetMapping("/modules/{id}")
    public ResponseEntity<ApiResponse<Object>> getModuleById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            ModuleDto moduleDto;
            if (isAdmin(userDetails)) {
                moduleDto = moduleService.getModuleById(id);
            } else {
                String username = userDetails.getUsername();
                moduleDto = moduleService.userGetModuleById(id, username);
            }

            return ResponseEntity.ok(new ApiResponse<>("success", "Module retrieved successfully", moduleDto.toApiResponse()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(
                new ApiResponse<>("error", e.getMessage(), null)
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(
                new ApiResponse<>("error", "Module not found", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ApiResponse<>("error", "Internal server error", null)
            );
        }
    }

    @PutMapping("/modules/{id}")
    @SuppressWarnings("UseSpecificCatch")
    public ResponseEntity<ApiResponse<Object>> updateModule(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam(required = false) MultipartFile pdf_content,
            @RequestParam(required = false) MultipartFile video_content,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (!isAdmin(userDetails)) {
                return ResponseEntity.status(403).body(
                    new ApiResponse<>("error", "Admin access required", null)
                );
            }

            ModuleDto existingModule = moduleService.getModuleById(id);

            String pdfUrl = existingModule.pdfContent();
            String videoUrl = existingModule.videoContent();

            if (pdf_content != null && !pdf_content.isEmpty()) {
                if (pdfUrl != null) {
                    fileStorageService.deleteFile(pdfUrl);
                }
                pdfUrl = fileStorageService.storeFile(pdf_content, "pdfs");
            }

            if (video_content != null && !video_content.isEmpty()) {
                if (videoUrl != null) {
                    fileStorageService.deleteFile(videoUrl);
                }
                videoUrl = fileStorageService.storeFile(video_content, "videos");
            }

            ModuleDto updatedModule = moduleService.updateModule(id, title, description, pdfUrl, videoUrl);

            return ResponseEntity.ok(new ApiResponse<>("success", "Module updated successfully", updatedModule.toApiResponse()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(
                new ApiResponse<>("error", "Module not found", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(400).body(
                new ApiResponse<>("error", e.getMessage(), null)
            );
        }
    }

    @DeleteMapping("/modules/{id}")
    @SuppressWarnings("UseSpecificCatch")
    public ResponseEntity<Void> deleteModule(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (!isAdmin(userDetails)) {
                return ResponseEntity.status(403).build();
            }
            
            ModuleDto module = moduleService.getModuleById(id);

            if (module.pdfContent() != null) {
                fileStorageService.deleteFile(module.pdfContent());
            }
            if (module.videoContent() != null) {
                fileStorageService.deleteFile(module.videoContent());
            }

            moduleService.deleteModule(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PatchMapping("/courses/{courseId}/modules/reorder")
    public ResponseEntity<ApiResponse<Object>> reorderModules(
            @PathVariable Long courseId,
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (!isAdmin(userDetails)) {
                return ResponseEntity.status(403).body(
                    new ApiResponse<>("error", "Admin access required", null)
                );
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> moduleOrder = (List<Map<String, Object>>) request.get("module_order");

            List<Map<String, Object>> updatedOrder = moduleService.reorderModules(courseId, moduleOrder);

            Map<String, Object> responseData = Map.of("module_order", updatedOrder);

            return ResponseEntity.ok(new ApiResponse<>("success", "Modules reordered successfully", responseData));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(
                new ApiResponse<>("error", "Course not found", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(400).body(
                new ApiResponse<>("error", e.getMessage(), null)
            );
        }
    }

    @PatchMapping("/modules/{id}/complete")
    public ResponseEntity<ApiResponse<Object>> completeModule(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String username = userDetails.getUsername();
            Map<String, Object> completionData = moduleService.completeModule(id, username);

            return ResponseEntity.ok(new ApiResponse<>("success", "Module marked as completed", completionData));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(
                new ApiResponse<>("error", e.getMessage(), null)
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(
                new ApiResponse<>("error", "Module not found", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(400).body(
                new ApiResponse<>("error", e.getMessage(), null)
            );
        }
    }

    private boolean isAdmin(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}
