package com.grocademy.repository;

import com.grocademy.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ModuleRepository extends JpaRepository<Module, Long> {
    List<Module> findAllByCourseIdOrderByModuleOrderAsc(Long courseId);
}
