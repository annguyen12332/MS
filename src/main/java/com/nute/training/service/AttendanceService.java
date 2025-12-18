package com.nute.training.service;

import com.nute.training.entity.Attendance;
import com.nute.training.entity.Enrollment;
import com.nute.training.entity.Schedule;
import com.nute.training.entity.User;
import com.nute.training.repository.AttendanceRepository;
import com.nute.training.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service: AttendanceService
 * Quản lý điểm danh
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EnrollmentRepository enrollmentRepository;

    /**
     * Tìm tất cả điểm danh
     */
    @Transactional(readOnly = true)
    public List<Attendance> findAll() {
        return attendanceRepository.findAll();
    }

    /**
     * Tìm điểm danh theo ID
     */
    @Transactional(readOnly = true)
    public Optional<Attendance> findById(Long id) {
        return attendanceRepository.findById(id);
    }

    /**
     * Tìm điểm danh theo buổi học
     */
    @Transactional(readOnly = true)
    public List<Attendance> findBySchedule(Schedule schedule) {
        return attendanceRepository.findBySchedule(schedule);
    }

    /**
     * Tìm điểm danh của học viên
     */
    @Transactional(readOnly = true)
    public List<Attendance> findByStudent(User student) {
        return attendanceRepository.findByStudent(student);
    }

    /**
     * Tìm điểm danh của học viên theo lớp
     */
    @Transactional(readOnly = true)
    public List<Attendance> findStudentAttendanceByClass(User student, Long classId) {
        return attendanceRepository.findStudentAttendanceByClass(student, classId);
    }

    /**
     * Điểm danh học viên
     * Business Rule:
     * - Mỗi học viên chỉ được điểm danh 1 lần cho 1 buổi học
     * - Học viên phải đã được duyệt vào lớp (enrollment APPROVED)
     */
    public Attendance markAttendance(Schedule schedule, User student,
                                      Attendance.AttendanceStatus status,
                                      User markedBy, String note) {
        log.info("Marking attendance for student: {} in schedule: {}",
                student.getUsername(), schedule.getId());

        // Check if already marked
        if (attendanceRepository.existsByScheduleAndStudent(schedule, student)) {
            throw new IllegalArgumentException(
                    "Học viên đã được điểm danh trong buổi học này");
        }

        // Verify student is enrolled and approved
        Optional<Enrollment> enrollment = enrollmentRepository
                .findByStudentAndClassEntity(student, schedule.getClassEntity());

        if (enrollment.isEmpty() ||
            enrollment.get().getStatus() != Enrollment.EnrollmentStatus.APPROVED) {
            throw new IllegalArgumentException(
                    "Học viên chưa được duyệt vào lớp này");
        }

        // Create attendance
        Attendance attendance = new Attendance();
        attendance.setSchedule(schedule);
        attendance.setStudent(student);
        attendance.setStatus(status);
        attendance.setNote(note);
        attendance.setMarkedBy(markedBy);
        attendance.setMarkedAt(LocalDateTime.now());

        Attendance saved = attendanceRepository.save(attendance);
        log.info("Attendance marked successfully with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Cập nhật điểm danh
     */
    public Attendance updateAttendance(Long id, Attendance.AttendanceStatus status, String note) {
        log.info("Updating attendance ID: {}", id);

        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy điểm danh với ID: " + id));

        attendance.setStatus(status);
        attendance.setNote(note);
        attendance.setMarkedAt(LocalDateTime.now());

        Attendance updated = attendanceRepository.save(attendance);
        log.info("Attendance updated successfully: {}", updated.getId());
        return updated;
    }

    /**
     * Điểm danh hàng loạt cho tất cả học viên trong lớp
     * Business Rule: Chỉ điểm danh cho học viên đã được APPROVED
     * Cập nhật: Nếu đã có điểm danh thì cập nhật, chưa có thì tạo mới
     */
    public void markAttendanceForAllStudents(Schedule schedule,
                                             Attendance.AttendanceStatus defaultStatus,
                                             User markedBy) {
        log.info("Marking attendance for all students in schedule: {}", schedule.getId());

        // Get all approved enrollments
        List<Enrollment> enrollments = enrollmentRepository
                .findApprovedEnrollmentsByClass(schedule.getClassEntity());

        int createdCount = 0;
        int updatedCount = 0;

        for (Enrollment enrollment : enrollments) {
            User student = enrollment.getStudent();

            // Kiểm tra xem đã có điểm danh chưa
            Optional<Attendance> existingAttendance = attendanceRepository
                    .findByScheduleAndStudent(schedule, student);

            if (existingAttendance.isPresent()) {
                // CẬP NHẬT điểm danh hiện có
                Attendance attendance = existingAttendance.get();
                attendance.setStatus(defaultStatus);
                attendance.setMarkedBy(markedBy);
                attendance.setMarkedAt(LocalDateTime.now());
                attendanceRepository.save(attendance);
                updatedCount++;
                log.debug("Updated attendance for student: {}", student.getUsername());
            } else {
                // TẠO MỚI điểm danh
                Attendance attendance = new Attendance();
                attendance.setSchedule(schedule);
                attendance.setStudent(student);
                attendance.setStatus(defaultStatus);
                attendance.setMarkedBy(markedBy);
                attendance.setMarkedAt(LocalDateTime.now());
                attendanceRepository.save(attendance);
                createdCount++;
                log.debug("Created attendance for student: {}", student.getUsername());
            }
        }

        log.info("Attendance summary - Created: {}, Updated: {}, Total: {}",
                createdCount, updatedCount, createdCount + updatedCount);
    }

    /**
     * Tính tỷ lệ điểm danh
     */
    @Transactional(readOnly = true)
    public Double calculateAttendanceRate(Long studentId, Long classId) {
        Double rate = attendanceRepository.calculateAttendanceRate(studentId, classId);
        return rate != null ? rate : 0.0;
    }

    /**
     * Đếm số buổi có mặt
     */
    @Transactional(readOnly = true)
    public Long countPresentAttendances(Long studentId, Long classId) {
        return attendanceRepository.countPresentAttendances(studentId, classId);
    }

    /**
     * Đếm số buổi vắng
     */
    @Transactional(readOnly = true)
    public Long countAbsentAttendances(Long studentId, Long classId) {
        return attendanceRepository.countAbsentAttendances(studentId, classId);
    }

    /**
     * Tìm học viên chưa điểm danh
     */
    @Transactional(readOnly = true)
    public List<User> findStudentsNotMarked(Schedule schedule) {
        return attendanceRepository.findStudentsNotMarkedInSchedule(
                schedule.getClassEntity(), schedule);
    }

    /**
     * Xóa điểm danh
     */
    public void deleteAttendance(Long id) {
        log.info("Deleting attendance ID: {}", id);

        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy điểm danh với ID: " + id));

        attendanceRepository.delete(attendance);
        log.info("Attendance deleted successfully: {}", id);
    }
}
