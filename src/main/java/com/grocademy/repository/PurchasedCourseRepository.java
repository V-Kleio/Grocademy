package com.grocademy.repository;

import com.grocademy.entity.PurchasedCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface PurchasedCourseRepository extends JpaRepository<PurchasedCourse, Long> {
    @Query("SELECT pc.course.id FROM PurchasedCourse pc WHERE pc.user.id = :userId")
    Set<Long> findCourseIdsByUserId(@Param("userId") Long userId);
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}
