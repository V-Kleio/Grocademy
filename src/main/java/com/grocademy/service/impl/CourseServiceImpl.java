package com.grocademy.service.impl;

import com.grocademy.dto.CourseDto;
import com.grocademy.entity.Course;
import com.grocademy.repository.CourseRepository;
import com.grocademy.repository.PurchasedCourseRepository;
import com.grocademy.service.CourseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final PurchasedCourseRepository purchasedCourseRepository;

    @Autowired
    public CourseServiceImpl(CourseRepository courseRepository, PurchasedCourseRepository purchasedCourseRepository) {
        this.courseRepository = courseRepository;
        this.purchasedCourseRepository = purchasedCourseRepository;
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
}
