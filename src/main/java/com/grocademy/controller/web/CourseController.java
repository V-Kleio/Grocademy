package com.grocademy.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.grocademy.dto.CourseDto;
import com.grocademy.entity.User;
import com.grocademy.repository.UserRepository;
import com.grocademy.service.CourseService;

@Controller
public class CourseController {
    private final CourseService courseService;
    private final UserRepository userRepository; 

    @Autowired
    public CourseController(CourseService courseService, UserRepository userRepository) {
        this.courseService = courseService;
        this.userRepository = userRepository;
    }

    @GetMapping("/courses")
    public String browseCourses(
            Model model,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int limit
    ) {
        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Pageable pageable = PageRequest.of(page, Math.min(limit, 50));
            Page<CourseDto> coursePage = courseService.findAllCourses(q, pageable, user.getId());

            model.addAttribute("coursePage", coursePage);
            model.addAttribute("query", q);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", limit);
            model.addAttribute("user", user);

            return "browse-courses";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading courses: " + e.getMessage());
            model.addAttribute("coursePage", Page.empty());
            model.addAttribute("query", q);
            model.addAttribute("currentPage", 0);
            model.addAttribute("pageSize", limit);

            return "browse-courses";
        }
    }

    @GetMapping("/courses/{id}")
    public String courseDetailPage(
        @PathVariable Long id,
        Model model,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            CourseDto course = courseService.findCourseDetailsById(id, user.getId());
            model.addAttribute("course", course);

            return "course-detail";
        } catch (Exception e) {
            model.addAttribute("error", "Course not found: " + e.getMessage());
            return "redirect:/courses";
        }
    }

    @PostMapping("/courses/{id}/buy")
    public String buyCourse(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails,
        RedirectAttributes redirectAttributes
    ) {
        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            courseService.buyCourse(id, user.getId());
            redirectAttributes.addFlashAttribute("success", "Course purchased successfully! You can now access all modules.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Purchase failed: " + e.getMessage());
        }

        return "redirect:/courses/" + id;
    }
}
