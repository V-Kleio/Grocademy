package com.grocademy.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import com.grocademy.service.CertificateService;
import com.grocademy.service.ModuleService;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ModuleServiceImpl implements ModuleService {
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final PurchasedCourseRepository purchasedCourseRepository;
    private final ModuleCompletedRepository moduleCompletedRepository;
    private final UserRepository userRepository;
    private final CertificateService certificateService;

    @Autowired
    public ModuleServiceImpl(
        CourseRepository courseRepository,
        ModuleRepository moduleRepository,
        PurchasedCourseRepository purchasedCourseRepository,
        ModuleCompletedRepository moduleCompletedRepository,
        UserRepository userRepository,
        CertificateService certificateService
    ) {
        this.courseRepository = courseRepository;
        this.moduleRepository = moduleRepository;
        this.purchasedCourseRepository = purchasedCourseRepository;
        this.moduleCompletedRepository = moduleCompletedRepository;
        this.userRepository = userRepository;
        this.certificateService = certificateService;
    }

    @Override
    @Transactional
    public ModuleDto createModule(Long courseId, String title, String description, String pdfUrl, String videoUrl) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new EntityNotFoundException("Course not found, ID: " + courseId));

        int nextOrder = moduleRepository.countByCourseId(courseId) + 1;

        Module module = new Module.Builder()
            .course(course)
            .title(title)
            .description(description)
            .pdfContentUrl(pdfUrl)
            .videoContentUrl(videoUrl)
            .moduleOrder(nextOrder)
            .build();

        Module savedModule = moduleRepository.save(module);
        return ModuleDto.fromEntity(savedModule);
    }

    @Override
    @Transactional
    public ModuleDto updateModule(Long id, String title, String description, String pdfUrl, String videoUrl) {
        Module module = moduleRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Module not found, ID: " + id));

        module.setTitle(title);
        module.setDescription(description);
        module.setPdfContentUrl(pdfUrl);
        module.setVideoContentUrl(videoUrl);

        Module savedModule = moduleRepository.save(module);
        return ModuleDto.fromEntity(savedModule);
    }

    @Override
    @Transactional
    public void deleteModule(Long id) {
        if (!moduleRepository.existsById(id)) {
            throw new EntityNotFoundException("Module not found, ID: " + id);
        }
        moduleRepository.deleteById(id);
    }

    @Override
    public Page<ModuleDto> getModulesByCourse(Long courseId, Pageable pageable) {
        if (!courseRepository.existsById(courseId)) {
            throw new EntityNotFoundException("Course not found, ID: " + courseId);
        }

        Page<Module> modulePage = moduleRepository.findAllByCourseIdOrderByModuleOrderAsc(courseId, pageable);
        return modulePage.map(ModuleDto::fromEntity);
    }

    @Override
    public ModuleDto getModuleById(Long id) {
        Module module = moduleRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Module not found, ID: " + id));
        return ModuleDto.fromEntity(module);
    }

    @Override
    public Page<ModuleDto> userGetModulesByCourse(Long courseId, String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        if (!purchasedCourseRepository.existsByUserIdAndCourseId(user.getId(), courseId)) {
            throw new SecurityException("User does not have access to this course.");
        }

        Page<Module> modulePage = moduleRepository.findAllByCourseIdOrderByModuleOrderAsc(courseId, pageable);
        Set<Long> completedModuleIds = moduleCompletedRepository.findCompletedModuleIdByUserIdAndCourseId(user.getId(), courseId);

        return modulePage.map(module -> ModuleDto.fromEntityWithUserData(
            module, 
            completedModuleIds.contains(module.getId())
        ));
    }

    @Override
    public List<ModuleDto> userGetModulesByCourse(Long courseId, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        if (!purchasedCourseRepository.existsByUserIdAndCourseId(user.getId(), courseId)) {
            throw new SecurityException("User does not have access to this course");
        }

        List<Module> modules = moduleRepository.findAllByCourseIdOrderByModuleOrderAsc(courseId);

        Set<Long> completedModuleIds = moduleCompletedRepository.findCompletedModuleIdByUserIdAndCourseId(user.getId(), courseId);

        return modules.stream()
            .map(module -> ModuleDto.fromEntityWithUserData(module, completedModuleIds.contains(module.getId())))
            .collect(Collectors.toList());
    }

    @Override
    public ModuleDto userGetModuleById(Long id, String username) {
        Module module = moduleRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Module not found, ID: " + id));

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        if (!purchasedCourseRepository.existsByUserIdAndCourseId(user.getId(), module.getCourse().getId())) {
            throw new SecurityException("User does not have access to this module.");
        }

        boolean isCompleted = moduleCompletedRepository.existsByUserIdAndModuleId(user.getId(), id);
        return ModuleDto.fromEntityWithUserData(module, isCompleted);
    }

    @Override
    @Transactional
    public Map<String, Object> completeModule(Long moduleId, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        Module module = moduleRepository.findById(moduleId)
            .orElseThrow(() -> new EntityNotFoundException("Module not found, ID: " + moduleId));

        if (!purchasedCourseRepository.existsByUserIdAndCourseId(user.getId(), module.getCourse().getId())) {
            throw new SecurityException("User do not own the module.");
        }

        if (!moduleCompletedRepository.existsByUserIdAndModuleId(user.getId(), moduleId)) {
            ModuleCompleted completion = new ModuleCompleted.Builder()
                .user(user)
                .module(module)
                .build();
            moduleCompletedRepository.save(completion);
        }

        Long courseId = module.getCourse().getId();
        int totalModules = moduleRepository.countByCourseId(courseId);
        Set<Long> completedModuleIds = moduleCompletedRepository.findCompletedModuleIdByUserIdAndCourseId(user.getId(), courseId);
        int completedModules = completedModuleIds.size();
        int percentage = totalModules > 0 ? (int) Math.round(((double) completedModules / totalModules) * 100) : 0;

        String certificateUrl = null;
        if (percentage == 100) {
            certificateUrl = certificateService.generateCertificate(user, module.getCourse());
        }

        return Map.of(
            "module_id", moduleId.toString(),
            "is_completed", true,
            "course_progress", Map.of(
                "total_modules", totalModules,
                "completed_modules", completedModules,
                "percentage", percentage
            ),
            "certificate_url", certificateUrl != null ? certificateUrl : ""
        );
    }

    @Override
    @Transactional
    public List<Map<String, Object>> reorderModules(Long courseId, List<Map<String, Object>> moduleOrder) {
        if (!courseRepository.existsById(courseId)) {
            throw new EntityNotFoundException("Course not found, ID: " + courseId);
        }

        for (Map<String, Object> item : moduleOrder) {
            String moduleIdStr = (String) item.get("id");
            Integer order = (Integer) item.get("order");

            Long moduleId = Long.valueOf(moduleIdStr);
            Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new EntityNotFoundException("Module not found, ID: " + moduleId));

            module.setModuleOrder(order);
            moduleRepository.save(module);
        }

        return moduleOrder;
    }

    @Override
    public CourseModuleDto getCourseModuleData(Long courseId, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        if (!purchasedCourseRepository.existsByUserIdAndCourseId(user.getId(), courseId)) {
            throw new SecurityException("User does not have access to this course.");
        }

        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new EntityNotFoundException("Course not found, ID: " + courseId));

        List<Module> modules = moduleRepository.findAllByCourseIdOrderByModuleOrderAsc(courseId);
        Set<Long> completedModuleIds = moduleCompletedRepository.findCompletedModuleIdByUserIdAndCourseId(user.getId(), courseId);

        List<ModuleDto> moduleDtos = modules.stream()
            .map(module -> ModuleDto.fromEntityWithUserData(
                module, 
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
    public void markModuleAsComplete(Long moduleId, String username) {
        completeModule(moduleId, username);
    }

    @Override
    public boolean hasUserCompletedAllModules(Long userId, Long courseId) {
        long totalModules = moduleRepository.countByCourseId(courseId);
        if (totalModules == 0) {
            return false;
        }
        long completedModules = moduleCompletedRepository.countCompletedByUserIdAndCourseId(userId, courseId);
        return totalModules == completedModules;
    }
}
