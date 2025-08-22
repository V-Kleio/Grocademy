package com.grocademy.service;

import com.grocademy.dto.CourseModuleDto;

public interface ModuleService {
    CourseModuleDto getCourseModuleData(Long courseId, Long userId);
    void markModuleAsComplete(Long moduleId, Long userId);
}
