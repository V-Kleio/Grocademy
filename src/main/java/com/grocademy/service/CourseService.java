package com.grocademy.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.grocademy.dto.CourseDto;
import com.grocademy.entity.Course;

public interface CourseService {
    Page<CourseDto> findAllCourses(String query, Pageable pageable, Long userId);
    CourseDto findCourseDetailsById(Long courseId, Long userId);
    void buyCourse(Long courseId, Long userId);
    Page<CourseDto> findPurchasedCourses(Long userId, String query, Pageable pageable);

    CourseDto createCourse(String title, String description, String instructor,
                           List<String> topics, BigDecimal price, String thumbnailImage);
    CourseDto updateCourse(Long id, String title, String description, String instructor,
                           List<String> topics, BigDecimal price, String thumbnailImage);
    void deleteCourse(Long id);

    Page<CourseDto> apiGetAllCourses(String query, Pageable pageable);
    CourseDto apiGetCourseById(Long id);

    int getTotalModules(Long courseId);
    Course getCourseEntityById(Long courseId);
}
