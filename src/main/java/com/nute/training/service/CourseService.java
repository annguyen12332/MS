package com.nute.training.service;

import com.nute.training.entity.Course;
import com.nute.training.entity.CourseType;
import com.nute.training.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service: CourseService
 * Quản lý khóa học
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;

    /**
     * Tìm tất cả khóa học
     */
    @Transactional(readOnly = true)
    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    /**
     * Tìm khóa học theo ID
     */
    @Transactional(readOnly = true)
    public Optional<Course> findById(Long id) {
        return courseRepository.findById(id);
    }

    /**
     * Tìm khóa học theo code
     */
    @Transactional(readOnly = true)
    public Optional<Course> findByCode(String code) {
        return courseRepository.findByCode(code);
    }

    /**
     * Tìm khóa học theo trạng thái
     */
    @Transactional(readOnly = true)
    public List<Course> findByStatus(Course.CourseStatus status) {
        return courseRepository.findByStatus(status);
    }

    /**
     * Tìm khóa học ACTIVE
     */
    @Transactional(readOnly = true)
    public List<Course> findActiveCourses() {
        return courseRepository.findByStatusOrderByCreatedAtDesc(Course.CourseStatus.ACTIVE);
    }

    /**
     * Tìm khóa học theo loại
     */
    @Transactional(readOnly = true)
    public List<Course> findByCourseType(CourseType courseType) {
        return courseRepository.findByCourseType(courseType);
    }

    /**
     * Tìm kiếm khóa học
     */
    @Transactional(readOnly = true)
    public List<Course> searchCourses(String keyword) {
        return courseRepository.searchCourses(keyword);
    }

    /**
     * Tạo khóa học mới
     * Business Rule:
     * - Code phải unique
     * - Duration hours và sessions phải > 0
     * - Tuition fee phải >= 0
     * - Max students phải >= 1
     */
    public Course createCourse(Course course) {
        log.info("Creating new course: {}", course.getCode());

        // Validate code unique
        if (courseRepository.existsByCode(course.getCode())) {
            throw new IllegalArgumentException(
                    "Mã khóa học đã tồn tại: " + course.getCode());
        }

        // Validate business rules
        validateCourseData(course);

        // Set default status
        if (course.getStatus() == null) {
            course.setStatus(Course.CourseStatus.DRAFT);
        }

        Course saved = courseRepository.save(course);
        log.info("Course created successfully: {}", saved.getCode());
        return saved;
    }

    /**
     * Cập nhật khóa học
     */
    public Course updateCourse(Long id, Course courseDetails) {
        log.info("Updating course: {}", id);

        Course existing = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy khóa học với ID: " + id));

        // Validate code unique (nếu thay đổi code)
        if (!existing.getCode().equals(courseDetails.getCode())) {
            if (courseRepository.existsByCode(courseDetails.getCode())) {
                throw new IllegalArgumentException(
                        "Mã khóa học đã tồn tại: " + courseDetails.getCode());
            }
            existing.setCode(courseDetails.getCode());
        }

        // Validate business rules
        validateCourseData(courseDetails);

        // Update fields
        existing.setCourseType(courseDetails.getCourseType());
        existing.setName(courseDetails.getName());
        existing.setDescription(courseDetails.getDescription());
        existing.setDurationHours(courseDetails.getDurationHours());
        existing.setDurationSessions(courseDetails.getDurationSessions());
        existing.setTuitionFee(courseDetails.getTuitionFee());
        existing.setMaxStudents(courseDetails.getMaxStudents());
        existing.setRequirements(courseDetails.getRequirements());
        existing.setStatus(courseDetails.getStatus());

        Course updated = courseRepository.save(existing);
        log.info("Course updated successfully: {}", updated.getCode());
        return updated;
    }

    /**
     * Thay đổi trạng thái khóa học
     */
    public void changeStatus(Long courseId, Course.CourseStatus status) {
        log.info("Changing course status for ID: {} to {}", courseId, status);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy khóa học với ID: " + courseId));

        course.setStatus(status);
        courseRepository.save(course);

        log.info("Course status changed successfully: {}", course.getCode());
    }

    /**
     * Xóa khóa học
     */
    public void deleteCourse(Long id) {
        log.info("Deleting course: {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy khóa học với ID: " + id));

        courseRepository.delete(course);
        log.info("Course deleted successfully: {}", course.getCode());
    }

    /**
     * Validate dữ liệu khóa học
     */
    private void validateCourseData(Course course) {
        if (course.getDurationHours() != null && course.getDurationHours() <= 0) {
            throw new IllegalArgumentException("Thời lượng khóa học phải lớn hơn 0");
        }

        if (course.getDurationSessions() != null && course.getDurationSessions() <= 0) {
            throw new IllegalArgumentException("Số buổi học phải lớn hơn 0");
        }

        if (course.getTuitionFee() != null && course.getTuitionFee().signum() < 0) {
            throw new IllegalArgumentException("Học phí phải >= 0");
        }

        if (course.getMaxStudents() != null && course.getMaxStudents() <= 0) {
            throw new IllegalArgumentException("Sĩ số tối đa phải >= 1");
        }
    }

    /**
     * Kiểm tra code đã tồn tại chưa
     */
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return courseRepository.existsByCode(code);
    }
}
