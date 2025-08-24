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

@Controller
public class DashboardController {
    private final CourseService courseService;
    private final UserRepository userRepository;

    @Autowired
    public DashboardController(CourseService courseService, UserRepository userRepository) {
        this.courseService = courseService;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable recentCoursesPageable = PageRequest.of(0, 6);
        var recentCourses = courseService.findPurchasedCourses(currentUser.getId(), "", recentCoursesPageable);

        Pageable allCoursesPageable = PageRequest.of(0, 6);
        var allCourses = courseService.findAllCourses("", allCoursesPageable, currentUser.getId());

        model.addAttribute("user", currentUser);
        model.addAttribute("recentCourses", recentCourses);
        model.addAttribute("allCourses", allCourses);
        model.addAttribute("totalPurchasedCourses", recentCourses.getTotalElements());

        return "dashboard";
    }

    @GetMapping("/")
    public String home(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }
}
