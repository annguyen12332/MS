package com.nute.training.service;

import com.nute.training.entity.ClassEntity;
import com.nute.training.entity.Course;
import com.nute.training.entity.User;
import com.nute.training.repository.ClassRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service: ClassService
 * Quản lý lớp học
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClassService {

    private final ClassRepository classRepository;

    /**
     * Tìm tất cả lớp học
     */
    @Transactional(readOnly = true)
    public List<ClassEntity> findAll() {
        return classRepository.findAll();
    }

    /**
     * Tìm lớp theo ID
     */
    @Transactional(readOnly = true)
    public Optional<ClassEntity> findById(Long id) {
        return classRepository.findById(id);
    }

    /**
     * Tìm lớp theo class code
     */
    @Transactional(readOnly = true)
    public Optional<ClassEntity> findByClassCode(String classCode) {
        return classRepository.findByClassCode(classCode);
    }

    /**
     * Tìm lớp theo khóa học
     */
    @Transactional(readOnly = true)
    public List<ClassEntity> findByCourse(Course course) {
        return classRepository.findByCourse(course);
    }

    /**
     * Tìm lớp theo giảng viên
     */
    @Transactional(readOnly = true)
    public List<ClassEntity> findByTeacher(User teacher) {
        return classRepository.findByTeacher(teacher);
    }

    /**
     * Tìm lớp theo trạng thái
     */
    @Transactional(readOnly = true)
    public List<ClassEntity> findByStatus(ClassEntity.ClassStatus status) {
        return classRepository.findByStatus(status);
    }

    /**
     * Tìm lớp đang diễn ra của giảng viên
     */
    @Transactional(readOnly = true)
    public List<ClassEntity> findOngoingClassesByTeacher(User teacher) {
        return classRepository.findOngoingClassesByTeacher(teacher);
    }

    /**
     * Tìm lớp có thể đăng ký (chưa đầy)
     */
    @Transactional(readOnly = true)
    public List<ClassEntity> findAvailableClasses() {
        return classRepository.findAvailableClasses();
    }

    /**
     * Tìm kiếm lớp
     */
    @Transactional(readOnly = true)
    public List<ClassEntity> searchClasses(String keyword) {
        return classRepository.searchClasses(keyword);
    }

    /**
     * Tạo lớp học mới
     * Business Rule:
     * - Class code phải unique
     * - Ngày kết thúc phải sau ngày bắt đầu
     * - Max students phải >= 1
     * - Giảng viên phải có role TEACHER
     */
    public ClassEntity createClass(ClassEntity classEntity) {
        log.info("Creating new class: {}", classEntity.getClassCode());

        // Validate class code unique
        if (classRepository.existsByClassCode(classEntity.getClassCode())) {
            throw new IllegalArgumentException(
                    "Mã lớp đã tồn tại: " + classEntity.getClassCode());
        }

        // Validate date range
        validateDateRange(classEntity.getStartDate(), classEntity.getEndDate());

        // Validate max students
        if (classEntity.getMaxStudents() != null && classEntity.getMaxStudents() <= 0) {
            throw new IllegalArgumentException("Sĩ số tối đa phải >= 1");
        }

        // Validate teacher role
        if (classEntity.getTeacher() != null) {
            if (classEntity.getTeacher().getRole() != User.Role.TEACHER) {
                throw new IllegalArgumentException("Người được phân công phải là giảng viên");
            }
        }

        // Set default values
        if (classEntity.getCurrentStudents() == null) {
            classEntity.setCurrentStudents(0);
        }

        if (classEntity.getStatus() == null) {
            classEntity.setStatus(ClassEntity.ClassStatus.PENDING);
        }

        ClassEntity saved = classRepository.save(classEntity);
        log.info("Class created successfully: {}", saved.getClassCode());
        return saved;
    }

    /**
     * Cập nhật lớp học
     */
    public ClassEntity updateClass(Long id, ClassEntity classDetails) {
        log.info("Updating class: {}", id);

        ClassEntity existing = classRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy lớp học với ID: " + id));

        // Validate class code unique (nếu thay đổi)
        if (!existing.getClassCode().equals(classDetails.getClassCode())) {
            if (classRepository.existsByClassCode(classDetails.getClassCode())) {
                throw new IllegalArgumentException(
                        "Mã lớp đã tồn tại: " + classDetails.getClassCode());
            }
            existing.setClassCode(classDetails.getClassCode());
        }

        // Validate date range
        validateDateRange(classDetails.getStartDate(), classDetails.getEndDate());

        // Validate teacher role
        if (classDetails.getTeacher() != null) {
            if (classDetails.getTeacher().getRole() != User.Role.TEACHER) {
                throw new IllegalArgumentException("Người được phân công phải là giảng viên");
            }
        }

        // Update fields
        existing.setCourse(classDetails.getCourse());
        existing.setTeacher(classDetails.getTeacher());
        existing.setClassName(classDetails.getClassName());
        existing.setStartDate(classDetails.getStartDate());
        existing.setEndDate(classDetails.getEndDate());
        existing.setMaxStudents(classDetails.getMaxStudents());
        existing.setRoom(classDetails.getRoom());
        existing.setStatus(classDetails.getStatus());

        ClassEntity updated = classRepository.save(existing);
        log.info("Class updated successfully: {}", updated.getClassCode());
        return updated;
    }

    /**
     * Phân công giảng viên
     * Business Rule: Giảng viên phải có role TEACHER
     */
    public void assignTeacher(Long classId, User teacher) {
        log.info("Assigning teacher to class ID: {}", classId);

        if (teacher.getRole() != User.Role.TEACHER) {
            throw new IllegalArgumentException("Người được phân công phải là giảng viên");
        }

        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy lớp học với ID: " + classId));

        classEntity.setTeacher(teacher);
        classRepository.save(classEntity);

        log.info("Teacher assigned successfully to class: {}", classEntity.getClassCode());
    }

    /**
     * Thay đổi trạng thái lớp
     */
    public void changeStatus(Long classId, ClassEntity.ClassStatus status) {
        log.info("Changing class status for ID: {} to {}", classId, status);

        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy lớp học với ID: " + classId));

        classEntity.setStatus(status);
        classRepository.save(classEntity);

        log.info("Class status changed successfully: {}", classEntity.getClassCode());
    }

    /**
     * Tăng số lượng học viên hiện tại
     * Business Rule: Không được vượt quá max students
     */
    public void incrementCurrentStudents(Long classId) {
        log.info("Incrementing current students for class ID: {}", classId);

        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy lớp học với ID: " + classId));

        if (classEntity.isFull()) {
            throw new IllegalStateException("Lớp học đã đầy");
        }

        classEntity.setCurrentStudents(classEntity.getCurrentStudents() + 1);
        classRepository.save(classEntity);

        log.info("Current students incremented for class: {}", classEntity.getClassCode());
    }

    /**
     * Giảm số lượng học viên hiện tại
     */
    public void decrementCurrentStudents(Long classId) {
        log.info("Decrementing current students for class ID: {}", classId);

        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy lớp học với ID: " + classId));

        if (classEntity.getCurrentStudents() > 0) {
            classEntity.setCurrentStudents(classEntity.getCurrentStudents() - 1);
            classRepository.save(classEntity);
        }

        log.info("Current students decremented for class: {}", classEntity.getClassCode());
    }

    /**
     * Xóa lớp học
     */
    public void deleteClass(Long id) {
        log.info("Deleting class: {}", id);

        ClassEntity classEntity = classRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy lớp học với ID: " + id));

        classRepository.delete(classEntity);
        log.info("Class deleted successfully: {}", classEntity.getClassCode());
    }

    /**
     * Validate date range
     */
    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
                throw new IllegalArgumentException(
                        "Ngày kết thúc phải sau ngày bắt đầu");
            }
        }
    }

    /**
     * Kiểm tra class code đã tồn tại chưa
     */
    @Transactional(readOnly = true)
    public boolean existsByClassCode(String classCode) {
        return classRepository.existsByClassCode(classCode);
    }
}
