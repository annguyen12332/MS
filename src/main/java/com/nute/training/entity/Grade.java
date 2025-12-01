package com.nute.training.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Entity: Grade
 * Điểm số
 */
@Entity
@Table(name = "grades")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false, unique = true)
    @NotNull(message = "Đăng ký học không được để trống")
    private Enrollment enrollment;

    @Column(name = "attendance_score", precision = 5, scale = 2)
    @DecimalMin(value = "0.00", message = "Điểm chuyên cần phải >= 0")
    @DecimalMax(value = "10.00", message = "Điểm chuyên cần phải <= 10")
    private BigDecimal attendanceScore;

    @Column(name = "process_score", precision = 5, scale = 2)
    @DecimalMin(value = "0.00", message = "Điểm quá trình phải >= 0")
    @DecimalMax(value = "10.00", message = "Điểm quá trình phải <= 10")
    private BigDecimal processScore;

    @Column(name = "final_score", precision = 5, scale = 2)
    @DecimalMin(value = "0.00", message = "Điểm thi cuối kỳ phải >= 0")
    @DecimalMax(value = "10.00", message = "Điểm thi cuối kỳ phải <= 10")
    private BigDecimal finalScore;

    @Column(name = "total_score", precision = 5, scale = 2)
    @DecimalMin(value = "0.00", message = "Điểm tổng kết phải >= 0")
    @DecimalMax(value = "10.00", message = "Điểm tổng kết phải <= 10")
    private BigDecimal totalScore;

    @Column(name = "grade_letter", length = 5)
    private String gradeLetter;

    @Column(name = "pass")
    private Boolean pass = false;

    @Column(columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "graded_by")
    private User gradedBy;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Business Rule: Tính điểm tổng kết
     * Công thức: Điểm chuyên cần 10% + Điểm quá trình 30% + Điểm cuối kỳ 60%
     */
    public void calculateTotalScore() {
        if (attendanceScore != null && processScore != null && finalScore != null) {
            BigDecimal attendance = attendanceScore.multiply(new BigDecimal("0.1"));
            BigDecimal process = processScore.multiply(new BigDecimal("0.3"));
            BigDecimal finalExam = finalScore.multiply(new BigDecimal("0.6"));

            this.totalScore = attendance.add(process).add(finalExam)
                    .setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * Business Rule: Xác định xếp loại (A, B, C, D, F)
     */
    public void calculateGradeLetter() {
        if (totalScore == null) {
            this.gradeLetter = null;
            return;
        }

        double score = totalScore.doubleValue();
        if (score >= 8.5) {
            this.gradeLetter = "A";
        } else if (score >= 7.0) {
            this.gradeLetter = "B";
        } else if (score >= 5.5) {
            this.gradeLetter = "C";
        } else if (score >= 4.0) {
            this.gradeLetter = "D";
        } else {
            this.gradeLetter = "F";
        }
    }

    /**
     * Business Rule: Xác định đạt/không đạt
     * Điều kiện: Điểm tổng >= 5.0
     */
    public void calculatePass() {
        if (totalScore == null) {
            this.pass = false;
            return;
        }
        this.pass = totalScore.compareTo(new BigDecimal("5.0")) >= 0;
    }

    /**
     * Business Rule: Tính toán tất cả các điểm
     */
    public void calculateAll() {
        calculateTotalScore();
        calculateGradeLetter();
        calculatePass();
    }
}
