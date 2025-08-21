package com.grocademy.controller.web;

import com.grocademy.entity.User;
import com.grocademy.repository.UserRepository;
import com.grocademy.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
        Long currentUserId = currentUser.getId();

        Pageable pageable = PageRequest.of(page, limit);
        model.addAttribute("coursePage", courseService.findAllCourses(query, pageable, currentUserId));
        model.addAttribute("query", query);

        return "browse-courses";
    }

    @GetMapping("/courses/{id}")
    public String courseDetailPage(
        @PathVariable Long id,
        Model model,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("course", courseService.findCourseDetailsById(id, currentUser.getId()));
        return "course-detail";
    }
}
