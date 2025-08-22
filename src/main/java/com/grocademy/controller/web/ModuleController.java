package com.grocademy.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.grocademy.entity.Module;
import com.grocademy.entity.User;
import com.grocademy.repository.ModuleRepository;
import com.grocademy.repository.UserRepository;
import com.grocademy.service.ModuleService;

@Controller
public class ModuleController {
    private final ModuleService moduleService;
    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;

    @Autowired
    public ModuleController(ModuleService moduleService, ModuleRepository moduleRepository, UserRepository userRepository) {
        this.moduleService = moduleService;
        this.moduleRepository = moduleRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/courses/{courseId}/modules")
    public String showModulePage(
        @PathVariable Long courseId,
        Model model,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("data", moduleService.getCourseModuleData(courseId, currentUser.getId()));
        return "course-modules";
    }

    @PostMapping("/modules/{moduleId}/complete")
    public String markModuleAsComplete(
        @PathVariable Long moduleId,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
        moduleService.markModuleAsComplete(moduleId, currentUser.getId());

        Module module = moduleRepository.findById(moduleId)
            .orElseThrow(() -> new IllegalArgumentException("Module not found, ID: " + moduleId));
        return "redirect:/courses/" + module.getCourse().getId() + "/modules";
    }
}
