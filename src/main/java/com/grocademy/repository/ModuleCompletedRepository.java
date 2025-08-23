package com.grocademy.repository;

import com.grocademy.entity.ModuleCompleted;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Set;

public interface ModuleCompletedRepository extends JpaRepository<ModuleCompleted, Long> {
    @Query("SELECT mc.module.id FROM ModuleCompleted mc " +
           "WHERE mc.user.id = :userId AND mc.module.course.id = :courseId")
    Set<Long> findCompletedModuleIdByUserIdAndCourseId(
        @Param("userId") Long userId,
        @Param("courseId") Long courseId
    );
    boolean existsByUserIdAndModuleId(Long userId, Long moduleId);
}
