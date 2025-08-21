package com.grocademy.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.grocademy.dto.UserDto;
import com.grocademy.service.UserService;

@Controller
public class AuthController {
    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserDto("", "", "", "", ""));
        return "register";
    }

    @PostMapping("/register")
    public String registerUserAccount(@ModelAttribute("user") UserDto registrationDto) {
        try {
            userService.registerUser(
                registrationDto.firstName(),
                registrationDto.lastName(),
                registrationDto.username(),
                registrationDto.email(),
                registrationDto.password()
            );
        } catch (IllegalStateException e) {
            return "redirect:/register?error";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }
}
