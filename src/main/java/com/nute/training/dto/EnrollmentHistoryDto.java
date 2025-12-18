package com.nute.training.dto;

import com.nute.training.entity.Enrollment;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO: EnrollmentHistoryDto
 * Projection cho enrollment history page - tối ưu performance
 */
@Data
@NoArgsConstructor
public class EnrollmentHistoryDto {

    // Enrollment fields
    private Long enrollmentId;
    private LocalDate enrollmentDate;
    private Enrollment.EnrollmentStatus status;
    private Enrollment.PaymentStatus paymentStatus;
    private BigDecimal paymentAmount;
    private String notes;
    private LocalDateTime approvedAt;

    // Class fields
    private Long classId;
    private String classCode;
    private String className;
    private String room;
    private LocalDate startDate;
    private LocalDate endDate;

    // Course fields
    private Long courseId;
    private String courseName;
    private String courseCode;
    private BigDecimal tuitionFee;

    // Teacher fields
    private Long teacherId;
    private String teacherName;
    private String teacherEmail;

    // Approved by fields (nullable)
    private Long approvedById;
    private String approvedByName;

    /**
     * Constructor for JPQL projection
     */
    public EnrollmentHistoryDto(
            Long enrollmentId,
            LocalDate enrollmentDate,
            Enrollment.EnrollmentStatus status,
            Enrollment.PaymentStatus paymentStatus,
            BigDecimal paymentAmount,
            String notes,
            LocalDateTime approvedAt,
            Long classId,
            String classCode,
            String className,
            String room,
            LocalDate startDate,
            LocalDate endDate,
            Long courseId,
            String courseName,
            String courseCode,
            BigDecimal tuitionFee,
            Long teacherId,
            String teacherName,
            String teacherEmail,
            Long approvedById,
            String approvedByName
    ) {
        this.enrollmentId = enrollmentId;
        this.enrollmentDate = enrollmentDate;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.paymentAmount = paymentAmount;
        this.notes = notes;
        this.approvedAt = approvedAt;
        this.classId = classId;
        this.classCode = classCode;
        this.className = className;
        this.room = room;
        this.startDate = startDate;
        this.endDate = endDate;
        this.courseId = courseId;
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.tuitionFee = tuitionFee;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.teacherEmail = teacherEmail;
        this.approvedById = approvedById;
        this.approvedByName = approvedByName;
    }

    /**
     * Helper method: Check if fully paid
     */
    public boolean isFullyPaid() {
        if (paymentAmount == null || tuitionFee == null) {
            return false;
        }
        return paymentAmount.compareTo(tuitionFee) >= 0;
    }

    /**
     * Helper method: Get status badge class
     */
    public String getStatusBadgeClass() {
        if (status == null) return "bg-secondary";
        return switch (status) {
            case PENDING -> "bg-warning bg-opacity-10 text-warning";
            case APPROVED -> "bg-success bg-opacity-10 text-success";
            case REJECTED -> "bg-danger bg-opacity-10 text-danger";
            case COMPLETED -> "bg-info bg-opacity-10 text-info";
            case DROPPED -> "bg-secondary bg-opacity-10 text-secondary";
        };
    }

    /**
     * Helper method: Get status icon
     */
    public String getStatusIcon() {
        if (status == null) return "fa-question-circle";
        return switch (status) {
            case PENDING -> "fa-clock";
            case APPROVED -> "fa-check-circle";
            case REJECTED -> "fa-times-circle";
            case COMPLETED -> "fa-graduation-cap";
            case DROPPED -> "fa-ban";
        };
    }

    /**
     * Helper method: Get status text
     */
    public String getStatusText() {
        if (status == null) return "Không xác định";
        return switch (status) {
            case PENDING -> "Chờ duyệt";
            case APPROVED -> "Đã duyệt";
            case REJECTED -> "Từ chối";
            case COMPLETED -> "Hoàn thành";
            case DROPPED -> "Đã hủy";
        };
    }
}
