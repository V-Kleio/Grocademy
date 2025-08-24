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
import org.springframework.web.bind.annotation.RequestParam;

import com.grocademy.dto.CourseDto;
import com.grocademy.entity.User;
import com.grocademy.repository.UserRepository;
import com.grocademy.service.CourseService;

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
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int limit) {
        try {
            User currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Pageable coursesPageable = PageRequest.of(page, limit);
            Page<CourseDto> myCourses = courseService.findPurchasedCourses(currentUser.getId(), query, coursesPageable);

            model.addAttribute("user", currentUser);
            model.addAttribute("myCourses", myCourses);
            model.addAttribute("query", query);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", limit);

            return "dashboard";
        } catch (Exception e) {
            System.err.println("Error in dashboard controller: " + e.getMessage());

            Page<CourseDto> emptyPage = Page.empty();

            model.addAttribute("myCourses", emptyPage);
            model.addAttribute("query", query);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", limit);

            return "dashboard";
        }
    }

    @GetMapping("/my-courses")
    public String myCoursesRedirect() {
        return "redirect:/dashboard";
    }

    @GetMapping("/")
    public String home(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }
}
