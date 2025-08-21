package com.grocademy.service.impl;

import com.grocademy.dto.CourseDetailedDto;
import com.grocademy.dto.CourseDto;
import com.grocademy.entity.Course;
import com.grocademy.entity.PurchasedCourse;
import com.grocademy.entity.User;
import com.grocademy.repository.CourseRepository;
import com.grocademy.repository.PurchasedCourseRepository;
import com.grocademy.repository.UserRepository;
import com.grocademy.service.CourseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

import java.util.Set;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final PurchasedCourseRepository purchasedCourseRepository;
    private final UserRepository userRepository;

    @Autowired
    public CourseServiceImpl(CourseRepository courseRepository, PurchasedCourseRepository purchasedCourseRepository, UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.purchasedCourseRepository = purchasedCourseRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Page<CourseDto> findAllCourses(String query, Pageable pageable, Long userId) {
        Set<Long> purchasedCourseIds = purchasedCourseRepository.findCourseIdsByUserId(userId);

        Page<Course> coursePage = courseRepository.findByTitleContainingIgnoreCaseOrInstructorContainingIgnoreCase(query, query, pageable);

        return coursePage.map(course -> new CourseDto(
            course.getId(),
            course.getTitle(),
            course.getInstructor(),
            course.getPrice(),
            course.getThumbnailImageUrl(),
            purchasedCourseIds.contains(course.getId())
        ));
    }

    @Override
    public CourseDetailedDto findCourseDetailsById(Long courseId, Long userId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new EntityNotFoundException("Course not found, ID: " + courseId));

        boolean isPurchased = purchasedCourseRepository.findCourseIdsByUserId(userId).contains(courseId);

        boolean isCompleted = false;

        return new CourseDetailedDto(
            course.getId(),
            course.getTitle(),
            course.getDescription(),
            course.getInstructor(),
            course.getTopics(),
            course.getPrice(),
            course.getThumbnailImageUrl(),
            isPurchased,
            isCompleted
        );
    }

    @Override
    @Transactional
    public void buyCourse(Long courseId, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found, ID: " + userId));
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new EntityNotFoundException("Course not found, ID: " + courseId));

        if (purchasedCourseRepository.findCourseIdsByUserId(userId).contains(courseId)) {
            throw new IllegalStateException("Course already purchased by user.");
        }

        if (user.getBalance().compareTo(course.getPrice()) < 0) {
            throw new IllegalStateException("Not enough money.");
        }

        user.subtractBalance(course.getPrice());
        PurchasedCourse purchase = new PurchasedCourse.Builder()
            .user(user)
            .course(course)
            .build();
        purchasedCourseRepository.save(purchase);
    }

    @Override
    public Page<CourseDto> findPurchasedCourses(Long userId, String query, Pageable pageable) {
        Page<Course> coursePage = courseRepository.findPurchasedCoursesByUserId(userId, query, pageable);

        return coursePage.map(course -> new CourseDto(
                course.getId(),
                course.getTitle(),
                course.getInstructor(),
                course.getPrice(),
                course.getThumbnailImageUrl(),
                true
        ));
    }
}
