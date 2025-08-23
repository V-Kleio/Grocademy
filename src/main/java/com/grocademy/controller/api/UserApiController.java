package com.grocademy.controller.api;

import com.grocademy.dto.ApiResponse;
import com.grocademy.dto.UserDto;
import com.grocademy.dto.UserUpdateDto;
import com.grocademy.dto.UserBalanceUpdateDto;
import com.grocademy.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Map;

import com.grocademy.dto.UserResponseDto;

@RestController
@RequestMapping("/api/users")
public class UserApiController {
    private final UserService userService;

    @Autowired
    public UserApiController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllUsers(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (!isAdmin(userDetails)) {
                return ResponseEntity.status(403).body(
                    new ApiResponse<>("error", "Admin access required", null)
                );
            }

            if (page < 1) page = 1;
            if (limit < 1 || limit > 50) limit = 15;

            Pageable pageable = PageRequest.of(page - 1, limit);
            Page<UserDto> userPage = userService.getAllUsers(q, pageable);

            List<UserResponseDto> userData = userPage.getContent().stream()
                .map(UserDto::toApiResponse)
                .toList();

            Map<String, Object> response = Map.of(
                "data", userData,
                "pagination", Map.of(
                    "current_page", page,
                    "total_pages", userPage.getTotalPages(),
                    "total_items", userPage.getTotalElements()
                )
            );

            return ResponseEntity.ok(new ApiResponse<>("success", "Users retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ApiResponse<>("error", "Internal server error", null)
            );
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> getUserById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (!isAdmin(userDetails)) {
                return ResponseEntity.status(403).body(
                    new ApiResponse<>("error", "Admin access required", null)
                );
            }

            UserDto userDto = userService.getUserById(id);

            return ResponseEntity.ok(new ApiResponse<>("success", "User retrieved successfully", userDto.toApiResponse()));
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

    @PostMapping("/{id}/balance")
    public ResponseEntity<ApiResponse<Object>> updateUserBalance(
            @PathVariable Long id,
            @RequestBody UserBalanceUpdateDto request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (!isAdmin(userDetails)) {
                return ResponseEntity.status(403).body(
                    new ApiResponse<>("error", "Admin access required", null)
                );
            }

            UserDto updatedUser = userService.updateUserBalance(id, request.increment());

            Map<String, Object> responseData = Map.of(
                "id", updatedUser.id().toString(),
                "username", updatedUser.username(),
                "balance", updatedUser.balance()
            );

            return ResponseEntity.ok(new ApiResponse<>("success", "User balance updated successfully", responseData));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(
                new ApiResponse<>("error", "User not found", null)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(
                new ApiResponse<>("error", e.getMessage(), null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ApiResponse<>("error", "Internal server error", null)
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateDto request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (!isAdmin(userDetails)) {
                return ResponseEntity.status(403).body(
                    new ApiResponse<>("error", "Admin access required", null)
                );
            }

            UserDto updatedUser = userService.updateUser(
                id, 
                request.email(), 
                request.username(), 
                request.firstName(), 
                request.lastName(), 
                request.password()
            );

            return ResponseEntity.ok(new ApiResponse<>("success", "User updated successfully", updatedUser.toApiResponse()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(
                new ApiResponse<>("error", "User not found", null)
            );
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(
                new ApiResponse<>("error", e.getMessage(), null)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(
                new ApiResponse<>("error", e.getMessage(), null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ApiResponse<>("error", "Internal server error", null)
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (!isAdmin(userDetails)) {
                return ResponseEntity.status(403).build();
            }

            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    private boolean isAdmin(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}
