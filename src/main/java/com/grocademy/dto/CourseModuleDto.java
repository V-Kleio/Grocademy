package com.grocademy.dto;

import java.util.List;

import com.grocademy.entity.Course;

public record CourseModuleDto(
    Course course,
    List<ModuleDto> modules,
    int progress
) {}
