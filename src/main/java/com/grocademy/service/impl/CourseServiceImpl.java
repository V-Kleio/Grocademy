package com.grocademy.service.impl;

import com.grocademy.dto.CourseDto;
import com.grocademy.entity.Course;
import com.grocademy.entity.PurchasedCourse;
import com.grocademy.entity.User;
import com.grocademy.repository.CourseRepository;
import com.grocademy.repository.PurchasedCourseRepository;
import com.grocademy.repository.UserRepository;
import com.grocademy.service.CourseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import com.grocademy.repository.ModuleCompletedRepository;
import com.grocademy.repository.ModuleRepository;

@Service
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;
    private final PurchasedCourseRepository purchasedCourseRepository;
    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;
    private final ModuleCompletedRepository moduleCompletedRepository;

    @Autowired
    public CourseServiceImpl(
        CourseRepository courseRepository,
        PurchasedCourseRepository purchasedCourseRepository,
        UserRepository userRepository,
        ModuleRepository moduleRepository,
        ModuleCompletedRepository moduleCompletedRepository
    ) {
        this.courseRepository = courseRepository;
        this.purchasedCourseRepository = purchasedCourseRepository;
        this.userRepository = userRepository;
        this.moduleRepository = moduleRepository;
        this.moduleCompletedRepository = moduleCompletedRepository;
    }

    @Override
    public Page<CourseDto> findAllCourses(String query, Pageable pageable, Long userId) {
        Set<Long> purchasedCourseIds = purchasedCourseRepository.findCourseIdsByUserId(userId);
        Page<Course> coursePage = courseRepository.findByTitleContainingIgnoreCaseOrInstructorContainingIgnoreCase(query, query, pageable);

        return coursePage.map(course -> {
            boolean isPurchased = purchasedCourseIds.contains(course.getId());
            int totalModules = getTotalModules(course.getId());
            boolean isCompleted = isPurchased && isAllModulesCompleted(course.getId(), userId);
            int progressPercentage = isPurchased ? calculateProgressPercentage(course.getId(), userId) : 0;

            return CourseDto.fromEntityWithUserData(
                course,
                isPurchased,
                isCompleted,
                totalModules,
                progressPercentage
            );
        });
    }

    @Override
    public CourseDto findCourseDetailsById(Long courseId, Long userId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new EntityNotFoundException("Course not found, ID: " + courseId));

        boolean isPurchased = purchasedCourseRepository.findCourseIdsByUserId(userId).contains(courseId);
        boolean isCompleted = isPurchased && isAllModulesCompleted(courseId, userId);
        int totalModules = getTotalModules(courseId);
        int progressPercentage = isPurchased ? calculateProgressPercentage(courseId, userId) : 0;

        return CourseDto.fromEntityWithUserData(
            course,
            isPurchased,
            isCompleted,
            totalModules,
            progressPercentage
        );
    }

    @Override
    @Transactional
    public void buyCourse(Long courseId, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found, ID: " + userId));
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new EntityNotFoundException("Course not found, ID: " + courseId));

        if (purchasedCourseRepository.findCourseIdsByUserId(userId).contains(courseId)) {
            throw new IllegalStateException("Course already purchased by user.");
        }

        if (user.getBalance().compareTo(course.getPrice()) < 0) {
            throw new IllegalStateException("Not enough money.");
        }

        user.subtractBalance(course.getPrice());
        PurchasedCourse purchase = new PurchasedCourse.Builder()
            .user(user)
            .course(course)
            .build();
        purchasedCourseRepository.save(purchase);
    }

    @Override
    public Page<CourseDto> findPurchasedCourses(Long userId, String query, Pageable pageable) {
        Page<Course> coursePage = courseRepository.findPurchasedCoursesByUserId(userId, query, pageable);

        return coursePage.map(course -> {
            int totalModules = getTotalModules(course.getId());
            boolean isCompleted = isAllModulesCompleted(course.getId(), userId);
            int progressPercentage = calculateProgressPercentage(course.getId(), userId);

            return CourseDto.fromEntityWithUserData(
                course,
                true,
                isCompleted,
                totalModules,
                progressPercentage
            );
        });
    }

    @Override
    @Transactional
    public CourseDto createCourse(String title, String description, String instructor,
                                  List<String> topics, BigDecimal price, String thumbnailImage) {
        Course course = new Course.Builder()
            .title(title)
            .description(description)
            .instructor(instructor)
            .topics(topics)
            .price(price)
            .thumbnailImageUrl(thumbnailImage)
            .build();

        Course savedCourse = courseRepository.save(course);
        return CourseDto.fromEntity(savedCourse);
    }

    @Override
    @Transactional
    public CourseDto updateCourse(Long id, String title, String description, String instructor,
                                  List<String> topics, BigDecimal price, String thumbnailImage) {
        Course course = courseRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Course not found, ID: " + id));

        course.setTitle(title);
        course.setDescription(description);
        course.setInstructor(instructor);
        course.setTopics(topics);
        course.setPrice(price);
        course.setThumbnailImageUrl(thumbnailImage);

        Course savedCourse = courseRepository.save(course);
        return CourseDto.fromEntity(savedCourse);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new EntityNotFoundException("Course not found, ID: " + id);
        }

        courseRepository.deleteById(id);
    }

    @Override
    public Page<CourseDto> apiGetAllCourses(String query, Pageable pageable) {
        Page<Course> coursePage;
        if (query == null || query.isEmpty()) {
            coursePage = courseRepository.findAll(pageable);
        } else {
            coursePage = courseRepository.findByTitleContainingIgnoreCaseOrInstructorContainingIgnoreCase(query, query, pageable);
        }

        return coursePage.map(course -> CourseDto.fromEntityWithUserData(
            course,
            false,
            false,
            getTotalModules(course.getId()),
            0
        ));
    }

    @Override
    public CourseDto apiGetCourseById(Long id) {
        Course course = courseRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Course not found, ID: " + id));

        return CourseDto.fromEntityWithUserData(
            course,
            false,
            false,
            getTotalModules(id),
            0
        );
    }

    @Override
    public int getTotalModules(Long courseId) {
        return moduleRepository.countByCourseId(courseId);
    }

    private boolean isAllModulesCompleted(Long courseId, Long userId) {
        int totalModules = getTotalModules(courseId);
        if (totalModules == 0) return false;

        Set<Long> completedModuleIds = moduleCompletedRepository.findCompletedModuleIdByUserIdAndCourseId(userId, courseId);
        return completedModuleIds.size() == totalModules;
    }

    private int calculateProgressPercentage(Long courseId, Long userId) {
        int totalModules = getTotalModules(courseId);
        if (totalModules == 0) return 0;

        Set<Long> completedModuleIds = moduleCompletedRepository.findCompletedModuleIdByUserIdAndCourseId(userId, courseId);

        return (int) Math.round(((double) completedModuleIds.size() / totalModules) * 100);
    }

    @Override
    public Course getCourseEntityById(Long courseId) {
        return courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
    }
}
