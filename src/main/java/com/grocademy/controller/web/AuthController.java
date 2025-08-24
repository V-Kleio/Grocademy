package com.grocademy.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.grocademy.controller.api.AuthApiController;
import com.grocademy.dto.ApiResponse;
import com.grocademy.dto.AuthRequestDto;
import com.grocademy.dto.AuthResponseDto;
import com.grocademy.dto.UserUpdateDto;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class AuthController {
    private final AuthApiController authApiController;

    @Autowired
    public AuthController(AuthApiController authApiController) {
        this.authApiController = authApiController;
    }

    @GetMapping({"/", "/auth", "/login", "/register"})
    public String showAuthPage(Model model) {
        model.addAttribute("user", new UserUpdateDto());
        return "auth";
    }

    @PostMapping("/login")
    @ResponseBody
    public ApiResponse<Object> webLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletResponse response) {
        try {
            AuthRequestDto authRequest = new AuthRequestDto(username, password);
            ResponseEntity<ApiResponse<AuthResponseDto>> apiResponse = authApiController.login(authRequest);
            ApiResponse<AuthResponseDto> responseBody = apiResponse.getBody();

            if (apiResponse.getStatusCode().is2xxSuccessful() && responseBody != null) {
                AuthResponseDto authData = responseBody.data();

                Cookie jwtCookie = new Cookie("jwt", authData.token());
                jwtCookie.setHttpOnly(true);
                jwtCookie.setSecure(true);
                jwtCookie.setPath("/");
                jwtCookie.setMaxAge(3600);
                response.addCookie(jwtCookie);

                return new ApiResponse<>("success", "Login successful", null);
            } else {
                return new ApiResponse<>("error", "Invalid credentials", null);
            }
        } catch (Exception e) {
            return new ApiResponse<>("error", "Login failed: " + e.getMessage(), null);
        }
    }

    @PostMapping("/register")
    @ResponseBody
    public ApiResponse<Object> webRegister(
            @ModelAttribute UserUpdateDto registrationDto,
            HttpServletResponse response) {
        try {
            ResponseEntity<ApiResponse<Object>> apiResponse = authApiController.register(registrationDto);

            if (apiResponse.getStatusCode().is2xxSuccessful() && apiResponse.getBody() != null) {
                AuthRequestDto authRequest = new AuthRequestDto(registrationDto.username(), registrationDto.password());
                ResponseEntity<ApiResponse<AuthResponseDto>> loginResponse = authApiController.login(authRequest);
                ApiResponse<AuthResponseDto> responseBody = loginResponse.getBody();

                if (loginResponse.getStatusCode().is2xxSuccessful() && responseBody != null) {
                    AuthResponseDto authData = responseBody.data();

                    Cookie jwtCookie = new Cookie("jwt", authData.token());
                    jwtCookie.setHttpOnly(true);
                    jwtCookie.setSecure(true);
                    jwtCookie.setPath("/");
                    jwtCookie.setMaxAge(3600);
                    response.addCookie(jwtCookie);
                }
                return new ApiResponse<>("success", "Registration successful", null);
            } else {
                return new ApiResponse<>("error", "Registration failed", null);
            }
        } catch (Exception e) {
            return new ApiResponse<>("error", "Registration failed: " + e.getMessage(), null);
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("jwt", "");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);

        return "redirect:/auth?logout=true";
    }
}
