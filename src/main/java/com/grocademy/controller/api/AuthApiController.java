package com.grocademy.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grocademy.dto.ApiResponse;
import com.grocademy.dto.AuthRequestDto;
import com.grocademy.dto.AuthResponseDto;
import com.grocademy.dto.UserDto;
import com.grocademy.dto.UserUpdateDto;
import com.grocademy.entity.User;
import com.grocademy.service.JwtService;
import com.grocademy.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserService userService;

    @Autowired
    public AuthApiController(
        AuthenticationManager authenticationManager,
        UserDetailsService userDetailsService,
        JwtService jwtService,
        UserService userService
    ) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> register(@RequestBody UserUpdateDto request) {
        try {
            User newUser = userService.registerUser(
                request.firstName(),
                request.lastName(),
                request.username(),
                request.email(),
                request.password()
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(newUser.getUsername());
            String token = jwtService.generateToken(userDetails);

            AuthResponseDto responseData = new AuthResponseDto(newUser.getUsername(), token);

            return ResponseEntity.ok(new ApiResponse<>("success", "User registered successfully", responseData));
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

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(@RequestBody AuthRequestDto request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.identifier(), request.password())
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.identifier());
            String token = jwtService.generateToken(userDetails);

            AuthResponseDto responseData = new AuthResponseDto(userDetails.getUsername(), token);
            return ResponseEntity.ok(new ApiResponse<>("success", "Login successful", responseData));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(
                new ApiResponse<>("error", "Invalid credentials", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ApiResponse<>("error", "Internal server error", null)
            );
        }
    }

    @GetMapping("/self")
    public ResponseEntity<ApiResponse<Object>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            UserDto userDto = userService.getCurrentUserInfo(userDetails.getUsername());

            return ResponseEntity.ok(new ApiResponse<>("success", "User info retrieved successfully", userDto.toApiResponse()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ApiResponse<>("error", "Internal server error", null)
            );
        }
    }
}
