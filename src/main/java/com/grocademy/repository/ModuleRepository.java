package com.grocademy.repository;

import com.grocademy.entity.Module;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ModuleRepository extends JpaRepository<Module, Long> {
    List<Module> findAllByCourseIdOrderByModuleOrderAsc(Long courseId);
    int countByCourseId(Long courseId);

    Page<Module> findAllByCourseIdOrderByModuleOrderAsc(@Param("courseId") Long courseId, Pageable pageable);
}
