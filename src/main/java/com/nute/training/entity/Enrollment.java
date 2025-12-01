package com.nute.training.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity: Enrollment
 * Đăng ký học
 */
@Entity
@Table(name = "enrollments",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "class_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @NotNull(message = "Học viên không được để trống")
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    @NotNull(message = "Lớp học không được để trống")
    private ClassEntity classEntity;

    @Column(name = "enrollment_date", nullable = false)
    @NotNull(message = "Ngày đăng ký không được để trống")
    private LocalDate enrollmentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status = EnrollmentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Column(name = "payment_amount", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "Số tiền thanh toán phải >= 0")
    private BigDecimal paymentAmount = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Enum: EnrollmentStatus
     * Trạng thái đăng ký
     */
    public enum EnrollmentStatus {
        PENDING,    // Chờ duyệt
        APPROVED,   // Đã duyệt
        REJECTED,   // Từ chối
        COMPLETED,  // Đã hoàn thành
        DROPPED     // Đã hủy
    }

    /**
     * Enum: PaymentStatus
     * Trạng thái thanh toán
     */
    public enum PaymentStatus {
        UNPAID,     // Chưa thanh toán
        PARTIAL,    // Thanh toán một phần
        PAID        // Đã thanh toán đủ
    }

    /**
     * Business Rule: Kiểm tra đã thanh toán đủ chưa
     */
    public boolean isFullyPaid(BigDecimal tuitionFee) {
        if (paymentAmount == null || tuitionFee == null) {
            return false;
        }
        return paymentAmount.compareTo(tuitionFee) >= 0;
    }
}
