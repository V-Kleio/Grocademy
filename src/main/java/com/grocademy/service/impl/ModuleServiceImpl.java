package com.grocademy.service.impl;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grocademy.dto.CourseModuleDto;
import com.grocademy.dto.ModuleDto;
import com.grocademy.entity.Course;
import com.grocademy.entity.Module;
import com.grocademy.entity.ModuleCompleted;
import com.grocademy.entity.User;
import com.grocademy.repository.CourseRepository;
import com.grocademy.repository.ModuleCompletedRepository;
import com.grocademy.repository.ModuleRepository;
import com.grocademy.repository.PurchasedCourseRepository;
import com.grocademy.repository.UserRepository;
import com.grocademy.service.ModuleService;

@Service
public class ModuleServiceImpl implements ModuleService {
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final PurchasedCourseRepository purchasedCourseRepository;
    private final ModuleCompletedRepository moduleCompletedRepository;
    private final UserRepository userRepository;

    @Autowired
    public ModuleServiceImpl(
        CourseRepository courseRepository,
        ModuleRepository moduleRepository,
        PurchasedCourseRepository purchasedCourseRepository,
        ModuleCompletedRepository moduleCompletedRepository,
        UserRepository userRepository
    ) {
        this.courseRepository = courseRepository;
        this.moduleRepository = moduleRepository;
        this.purchasedCourseRepository = purchasedCourseRepository;
        this.moduleCompletedRepository = moduleCompletedRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CourseModuleDto getCourseModuleData(Long courseId, Long userId) {
        if (!purchasedCourseRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new SecurityException("User does not have access to this course.");
        }

        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Course not found, ID: " + courseId));
        List<Module> modules = moduleRepository.findAllByCourseIdOrderByModuleOrderAsc(courseId);
        Set<Long> completedModuleIds = moduleCompletedRepository.findCompletedModuleIdByUserIdAndCourseId(userId, courseId);

        List<ModuleDto> moduleDtos = modules.stream()
            .map(module -> new ModuleDto(
                module.getId(),
                module.getTitle(),
                module.getModuleOrder(),
                completedModuleIds.contains(module.getId())
            ))
            .toList();

        int progress = 0;
        if (!modules.isEmpty()) {
            progress = (int) (((double) completedModuleIds.size() / modules.size()) * 100);
        }

        return new CourseModuleDto(course, moduleDtos, progress);
    }

    @Override
    @Transactional
    public void markModuleAsComplete(Long moduleId, Long userId) {
        Module module = moduleRepository.findById(moduleId)
            .orElseThrow(() -> new IllegalArgumentException("Module not found, ID: " + moduleId));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found, ID: " + userId));

        if (!purchasedCourseRepository.existsByUserIdAndCourseId(userId, module.getCourse().getId())) {
             throw new SecurityException("User cannot complete a module for a course they do not own.");
        }

        if (moduleCompletedRepository.existsByUserIdAndModuleId(userId, moduleId)) {
            return;
        }

        ModuleCompleted completion = new ModuleCompleted.Builder()
            .user(user)
            .module(module)
            .build();
        moduleCompletedRepository.save(completion);
    }
}
