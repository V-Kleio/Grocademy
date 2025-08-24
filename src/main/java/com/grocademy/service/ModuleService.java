package com.grocademy.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.grocademy.dto.CourseModuleDto;
import com.grocademy.dto.ModuleDto;

public interface ModuleService {
    ModuleDto createModule(Long courseId, String title, String description, String pdfUrl, String videoUrl);
    ModuleDto updateModule(Long id, String title, String description, String pdfUrl, String videoUrl);
    void deleteModule(Long id);

    Page<ModuleDto> getModulesByCourse(Long courseId, Pageable pageable);
    ModuleDto getModuleById(Long id);

    Page<ModuleDto> userGetModulesByCourse(Long courseId, String username, Pageable pageable);
    List<ModuleDto> userGetModulesByCourse(Long courseId, String username);
    ModuleDto userGetModuleById(Long id, String username);

    Map<String, Object> completeModule(Long moduleId, String username);
    
    List<Map<String, Object>> reorderModules(Long courseId, List<Map<String, Object>> moduleOrder);

    CourseModuleDto getCourseModuleData(Long courseId, String username);
    void markModuleAsComplete(Long moduleId, String username);

    boolean hasUserCompletedAllModules(Long userId, Long courseId);
}
