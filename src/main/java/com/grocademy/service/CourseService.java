package com.grocademy.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.grocademy.dto.CourseDetailedDto;
import com.grocademy.dto.CourseDto;

public interface CourseService {
    Page<CourseDto> findAllCourses(String query, Pageable pageable, Long userId);
    CourseDetailedDto findCourseDetailsById(Long courseId, Long userId);
    void buyCourse(Long courseId, Long userId);
}
