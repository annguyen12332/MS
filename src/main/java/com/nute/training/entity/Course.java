package com.nute.training.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity: Course
 * Khóa học
 */
@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_type_id")
    private CourseType courseType;

    @Column(unique = true, nullable = false, length = 20)
    @NotBlank(message = "Mã khóa học không được để trống")
    @Size(max = 20, message = "Mã khóa học không quá 20 ký tự")
    private String code;

    @Column(nullable = false, length = 200)
    @NotBlank(message = "Tên khóa học không được để trống")
    @Size(max = 200, message = "Tên khóa học không quá 200 ký tự")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_hours")
    @Min(value = 1, message = "Thời lượng phải lớn hơn 0 giờ")
    private Integer durationHours;

    @Column(name = "duration_sessions")
    @Min(value = 1, message = "Số buổi học phải lớn hơn 0")
    private Integer durationSessions;

    @Column(name = "tuition_fee", precision = 15, scale = 2)
    @DecimalMin(value = "0.00", message = "Học phí phải >= 0")
    private BigDecimal tuitionFee;

    @Column(name = "max_students")
    @Min(value = 1, message = "Sĩ số tối đa phải >= 1")
    private Integer maxStudents;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus status = CourseStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Enum: CourseStatus
     * Trạng thái khóa học
     */
    public enum CourseStatus {
        DRAFT,      // Nháp
        ACTIVE,     // Đang mở
        INACTIVE    // Đã đóng
    }
}
