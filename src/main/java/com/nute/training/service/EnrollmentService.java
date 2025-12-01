package com.nute.training.service;

import com.nute.training.entity.ClassEntity;
import com.nute.training.entity.Enrollment;
import com.nute.training.entity.User;
import com.nute.training.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service: EnrollmentService
 * Quản lý đăng ký học
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final ClassService classService;

    /**
     * Tìm tất cả đăng ký
     */
    @Transactional(readOnly = true)
    public List<Enrollment> findAll() {
        return enrollmentRepository.findAll();
    }

    /**
     * Tìm đăng ký theo ID
     */
    @Transactional(readOnly = true)
    public Optional<Enrollment> findById(Long id) {
        return enrollmentRepository.findById(id);
    }

    /**
     * Tìm đăng ký của học viên
     */
    @Transactional(readOnly = true)
    public List<Enrollment> findByStudent(User student) {
        return enrollmentRepository.findByStudent(student);
    }

    /**
     * Tìm đăng ký của lớp
     */
    @Transactional(readOnly = true)
    public List<Enrollment> findByClass(ClassEntity classEntity) {
        return enrollmentRepository.findByClassEntity(classEntity);
    }

    /**
     * Tìm đăng ký theo trạng thái
     */
    @Transactional(readOnly = true)
    public List<Enrollment> findByStatus(Enrollment.EnrollmentStatus status) {
        return enrollmentRepository.findByStatus(status);
    }

    /**
     * Tìm đăng ký PENDING (chờ duyệt)
     */
    @Transactional(readOnly = true)
    public List<Enrollment> findPendingEnrollments() {
        return enrollmentRepository.findPendingEnrollments();
    }

    /**
     * Tìm đăng ký đã duyệt của lớp
     */
    @Transactional(readOnly = true)
    public List<Enrollment> findApprovedEnrollmentsByClass(ClassEntity classEntity) {
        return enrollmentRepository.findApprovedEnrollmentsByClass(classEntity);
    }

    /**
     * Tạo đăng ký học mới (Student tự đăng ký)
     * Business Rule:
     * - Học viên chỉ được đăng ký 1 lần cho 1 lớp
     * - Lớp phải chưa đầy
     * - Lớp phải ở trạng thái PENDING hoặc ONGOING
     */
    public Enrollment createEnrollment(User student, ClassEntity classEntity, String notes) {
        log.info("Creating enrollment for student: {} in class: {}",
                student.getUsername(), classEntity.getClassCode());

        // Validate student role
        if (student.getRole() != User.Role.STUDENT) {
            throw new IllegalArgumentException("Chỉ học viên mới có thể đăng ký");
        }

        // Check if already enrolled
        if (enrollmentRepository.existsByStudentAndClassEntity(student, classEntity)) {
            throw new IllegalArgumentException(
                    "Bạn đã đăng ký lớp này rồi");
        }

        // Check if class is full
        if (classEntity.isFull()) {
            throw new IllegalStateException("Lớp học đã đầy");
        }

        // Check class status
        if (classEntity.getStatus() == ClassEntity.ClassStatus.COMPLETED ||
            classEntity.getStatus() == ClassEntity.ClassStatus.CANCELLED) {
            throw new IllegalStateException("Lớp học không còn nhận đăng ký");
        }

        // Create enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setClassEntity(classEntity);
        enrollment.setEnrollmentDate(LocalDate.now());
        enrollment.setStatus(Enrollment.EnrollmentStatus.PENDING);
        enrollment.setPaymentStatus(Enrollment.PaymentStatus.UNPAID);
        enrollment.setPaymentAmount(BigDecimal.ZERO);
        enrollment.setNotes(notes);

        Enrollment saved = enrollmentRepository.save(enrollment);
        log.info("Enrollment created successfully with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Duyệt đăng ký (Admin)
     * Business Rule:
     * - Chỉ duyệt đăng ký có status PENDING
     * - Lớp phải chưa đầy
     * - Tăng currentStudents của lớp
     */
    public Enrollment approveEnrollment(Long enrollmentId, User approvedBy) {
        log.info("Approving enrollment ID: {} by admin: {}",
                enrollmentId, approvedBy.getUsername());

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy đăng ký với ID: " + enrollmentId));

        // Validate status
        if (enrollment.getStatus() != Enrollment.EnrollmentStatus.PENDING) {
            throw new IllegalStateException(
                    "Chỉ có thể duyệt đăng ký có trạng thái PENDING");
        }

        // Check if class is full
        ClassEntity classEntity = enrollment.getClassEntity();
        if (classEntity.isFull()) {
            throw new IllegalStateException("Lớp học đã đầy");
        }

        // Update enrollment
        enrollment.setStatus(Enrollment.EnrollmentStatus.APPROVED);
        enrollment.setApprovedBy(approvedBy);
        enrollment.setApprovedAt(LocalDateTime.now());

        // Increment class current students
        classService.incrementCurrentStudents(classEntity.getId());

        Enrollment updated = enrollmentRepository.save(enrollment);
        log.info("Enrollment approved successfully: {}", updated.getId());
        return updated;
    }

    /**
     * Từ chối đăng ký (Admin)
     * Business Rule: Chỉ từ chối đăng ký có status PENDING
     */
    public Enrollment rejectEnrollment(Long enrollmentId, User rejectedBy, String reason) {
        log.info("Rejecting enrollment ID: {} by admin: {}",
                enrollmentId, rejectedBy.getUsername());

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy đăng ký với ID: " + enrollmentId));

        // Validate status
        if (enrollment.getStatus() != Enrollment.EnrollmentStatus.PENDING) {
            throw new IllegalStateException(
                    "Chỉ có thể từ chối đăng ký có trạng thái PENDING");
        }

        // Update enrollment
        enrollment.setStatus(Enrollment.EnrollmentStatus.REJECTED);
        enrollment.setApprovedBy(rejectedBy);
        enrollment.setApprovedAt(LocalDateTime.now());
        enrollment.setNotes(reason);

        Enrollment updated = enrollmentRepository.save(enrollment);
        log.info("Enrollment rejected successfully: {}", updated.getId());
        return updated;
    }

    /**
     * Hủy đăng ký (Student hoặc Admin)
     * Business Rule:
     * - Nếu đã APPROVED thì giảm currentStudents của lớp
     */
    public void cancelEnrollment(Long enrollmentId) {
        log.info("Cancelling enrollment ID: {}", enrollmentId);

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy đăng ký với ID: " + enrollmentId));

        // If already approved, decrement class current students
        if (enrollment.getStatus() == Enrollment.EnrollmentStatus.APPROVED) {
            classService.decrementCurrentStudents(enrollment.getClassEntity().getId());
        }

        enrollment.setStatus(Enrollment.EnrollmentStatus.DROPPED);
        enrollmentRepository.save(enrollment);

        log.info("Enrollment cancelled successfully: {}", enrollmentId);
    }

    /**
     * Cập nhật thanh toán
     */
    public Enrollment updatePayment(Long enrollmentId, BigDecimal amount) {
        log.info("Updating payment for enrollment ID: {}", enrollmentId);

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy đăng ký với ID: " + enrollmentId));

        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải >= 0");
        }

        enrollment.setPaymentAmount(amount);

        // Determine payment status
        BigDecimal tuitionFee = enrollment.getClassEntity().getCourse().getTuitionFee();
        if (enrollment.isFullyPaid(tuitionFee)) {
            enrollment.setPaymentStatus(Enrollment.PaymentStatus.PAID);
        } else if (amount.signum() > 0) {
            enrollment.setPaymentStatus(Enrollment.PaymentStatus.PARTIAL);
        } else {
            enrollment.setPaymentStatus(Enrollment.PaymentStatus.UNPAID);
        }

        Enrollment updated = enrollmentRepository.save(enrollment);
        log.info("Payment updated successfully for enrollment: {}", updated.getId());
        return updated;
    }

    /**
     * Hoàn thành khóa học
     */
    public void completeEnrollment(Long enrollmentId) {
        log.info("Completing enrollment ID: {}", enrollmentId);

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy đăng ký với ID: " + enrollmentId));

        if (enrollment.getStatus() != Enrollment.EnrollmentStatus.APPROVED) {
            throw new IllegalStateException(
                    "Chỉ có thể hoàn thành đăng ký có trạng thái APPROVED");
        }

        enrollment.setStatus(Enrollment.EnrollmentStatus.COMPLETED);
        enrollmentRepository.save(enrollment);

        log.info("Enrollment completed successfully: {}", enrollmentId);
    }

    /**
     * Đếm số học viên đã duyệt trong lớp
     */
    @Transactional(readOnly = true)
    public Long countApprovedEnrollmentsByClass(ClassEntity classEntity) {
        return enrollmentRepository.countApprovedEnrollmentsByClass(classEntity);
    }
}
