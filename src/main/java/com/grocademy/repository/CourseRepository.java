package com.grocademy.repository;

import com.grocademy.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Page<Course> findByTitleContainingIgnoreCaseOrInstructorContainingIgnoreCase(String title, String instructor, Pageable pageable);
    @Query("SELECT c FROM Course c JOIN PurchasedCourse pc ON c.id = pc.course.id " +
           "WHERE pc.user.id = :userId AND (" +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.instructor) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Course> findPurchasedCoursesByUserId(
        @Param("userId") Long userId, 
        @Param("query") String query, 
        Pageable pageable
    );
}
