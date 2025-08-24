package com.grocademy.controller.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.grocademy.dto.CourseDto;
import com.grocademy.dto.ModuleDto;
import com.grocademy.entity.Course;
import com.grocademy.entity.User;
import com.grocademy.repository.UserRepository;
import com.grocademy.service.CertificateService;
import com.grocademy.service.CourseService;
import com.grocademy.service.ModuleService;

@Controller
public class ModuleController {
    private final CourseService courseService;
    private final ModuleService moduleService;
    private final CertificateService certificateService;
    private final UserRepository userRepository;

    @Autowired
    public ModuleController(
        CourseService courseService,
        ModuleService moduleService,
        CertificateService certificateService,
        UserRepository userRepository
    ) {
        this.courseService = courseService;
        this.moduleService = moduleService;
        this.certificateService = certificateService;
        this.userRepository = userRepository;
    }

    @GetMapping("/courses/{id}/modules")
    public String courseModules(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            CourseDto course = courseService.findCourseDetailsById(id, user.getId());
            if (!course.isPurchased()) {
                redirectAttributes.addFlashAttribute("error", "You must purchase this course to view its modules.");
                return "redirect:/courses/" + id;
            }

            List<ModuleDto> modules = moduleService.userGetModulesByCourse(id, user.getUsername());

            long totalModules = modules.size();
            long completedModules = modules.stream().filter(ModuleDto::isCompleted).count();
            int progressPercentage = (totalModules > 0) ? (int) Math.round(((double) completedModules / totalModules) * 100) : 0;

            model.addAttribute("course", course);
            model.addAttribute("modules", modules);
            model.addAttribute("totalModules", totalModules);
            model.addAttribute("completedModules", completedModules);
            model.addAttribute("progressPercentage", progressPercentage);

            return "course-modules";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Could not load course modules: " + e.getMessage());
            return "redirect:/my-courses";
        }
    }

    @GetMapping("/courses/{id}/certificate")
    public ResponseEntity<?> downloadCertificate(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
        ) {
        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!moduleService.hasUserCompletedAllModules(user.getId(), id)) {
                return ResponseEntity.badRequest().body("Course not yet completed.");
            }

            Course courseEntity = courseService.getCourseEntityById(id);
            String certificate = certificateService.generateCertificate(user, courseEntity);
            
            String filename = "certificate-" + courseEntity.getTitle().replaceAll("\\s+", "_") + ".html";

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(certificate);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error generating certificate: " + e.getMessage());
        }
    }
}