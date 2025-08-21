package com.grocademy.repository;

import com.grocademy.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Page<Course> findByTitleContainingIgnoreCaseOrInstructorContainingIgnoreCase(String title, String instructor, Pageable pageable);
}
