package com.grocademy.repository;

import com.grocademy.entity.ModuleCompleted;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Set;

public interface ModuleCompletedRepository extends JpaRepository<ModuleCompleted, Long> {
    @Query("SELECT mc.module.id FROM ModuleCompletion mc WHERE mc.user.id = :userId AND mc.module.course.id = :courseId")
    Set<Long> findCompletedModuleIdByUserIdAndCourseId(Long userId, Long courseId);
    boolean existsByUserIdAndModuleId(Long userId, Long moduleId);
}
