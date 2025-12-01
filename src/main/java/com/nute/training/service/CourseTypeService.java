package com.nute.training.service;

import com.nute.training.entity.CourseType;
import com.nute.training.repository.CourseTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service: CourseTypeService
 * Quản lý loại khóa học
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CourseTypeService {

    private final CourseTypeRepository courseTypeRepository;

    /**
     * Tìm tất cả loại khóa học
     */
    @Transactional(readOnly = true)
    public List<CourseType> findAll() {
        return courseTypeRepository.findAll();
    }

    /**
     * Tìm loại khóa học theo ID
     */
    @Transactional(readOnly = true)
    public Optional<CourseType> findById(Long id) {
        return courseTypeRepository.findById(id);
    }

    /**
     * Tìm loại khóa học theo code
     */
    @Transactional(readOnly = true)
    public Optional<CourseType> findByCode(String code) {
        return courseTypeRepository.findByCode(code);
    }

    /**
     * Tạo loại khóa học mới
     * Business Rule: Code phải unique
     */
    public CourseType createCourseType(CourseType courseType) {
        log.info("Creating new course type: {}", courseType.getCode());

        // Validate code unique
        if (courseTypeRepository.existsByCode(courseType.getCode())) {
            throw new IllegalArgumentException(
                    "Mã loại khóa học đã tồn tại: " + courseType.getCode());
        }

        CourseType saved = courseTypeRepository.save(courseType);
        log.info("Course type created successfully: {}", saved.getCode());
        return saved;
    }

    /**
     * Cập nhật loại khóa học
     */
    public CourseType updateCourseType(Long id, CourseType courseTypeDetails) {
        log.info("Updating course type: {}", id);

        CourseType existing = courseTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy loại khóa học với ID: " + id));

        // Validate code unique (nếu thay đổi code)
        if (!existing.getCode().equals(courseTypeDetails.getCode())) {
            if (courseTypeRepository.existsByCode(courseTypeDetails.getCode())) {
                throw new IllegalArgumentException(
                        "Mã loại khóa học đã tồn tại: " + courseTypeDetails.getCode());
            }
            existing.setCode(courseTypeDetails.getCode());
        }

        existing.setName(courseTypeDetails.getName());
        existing.setDescription(courseTypeDetails.getDescription());

        CourseType updated = courseTypeRepository.save(existing);
        log.info("Course type updated successfully: {}", updated.getCode());
        return updated;
    }

    /**
     * Xóa loại khóa học
     */
    public void deleteCourseType(Long id) {
        log.info("Deleting course type: {}", id);

        CourseType courseType = courseTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy loại khóa học với ID: " + id));

        courseTypeRepository.delete(courseType);
        log.info("Course type deleted successfully: {}", courseType.getCode());
    }

    /**
     * Kiểm tra code đã tồn tại chưa
     */
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return courseTypeRepository.existsByCode(code);
    }
}
