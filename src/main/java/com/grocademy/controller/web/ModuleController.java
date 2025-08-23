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
import com.grocademy.repository.ModuleRepository;
import com.grocademy.service.ModuleService;

@Controller
public class ModuleController {
    private final ModuleService moduleService;
    private final ModuleRepository moduleRepository;

    @Autowired
    public ModuleController(ModuleService moduleService, ModuleRepository moduleRepository) {
        this.moduleService = moduleService;
        this.moduleRepository = moduleRepository;
    }

    @GetMapping("/courses/{courseId}/modules")
    public String showModulePage(
        @PathVariable Long courseId,
        Model model,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        model.addAttribute("data", moduleService.getCourseModuleData(courseId, userDetails.getUsername()));
        return "course-modules";
    }

    @PostMapping("/modules/{moduleId}/complete")
    public String markModuleAsComplete(
        @PathVariable Long moduleId,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        moduleService.markModuleAsComplete(moduleId, userDetails.getUsername());

        Module module = moduleRepository.findById(moduleId)
            .orElseThrow(() -> new IllegalArgumentException("Module not found, ID: " + moduleId));
        return "redirect:/courses/" + module.getCourse().getId() + "/modules";
    }
}
