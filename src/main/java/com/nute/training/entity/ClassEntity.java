package com.nute.training.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity: ClassEntity
 * Lớp học
 */
@Entity
@Table(name = "classes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @NotNull(message = "Khóa học không được để trống")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    @NotNull(message = "Giảng viên không được để trống")
    private User teacher;

    @Column(name = "class_code", unique = true, nullable = false, length = 20)
    @NotBlank(message = "Mã lớp không được để trống")
    @Size(max = 20, message = "Mã lớp không quá 20 ký tự")
    private String classCode;

    @Column(name = "class_name", length = 200)
    private String className;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "max_students")
    @Min(value = 1, message = "Sĩ số tối đa phải >= 1")
    private Integer maxStudents;

    @Column(name = "current_students")
    private Integer currentStudents = 0;

    @Column(length = 50)
    private String room;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClassStatus status = ClassStatus.PENDING;

    @OneToMany(mappedBy = "classEntity", fetch = FetchType.LAZY)
    private java.util.List<Enrollment> enrollments;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Enum: ClassStatus
     * Trạng thái lớp học
     */
    public enum ClassStatus {
        PENDING,    // Chưa bắt đầu
        ONGOING,    // Đang diễn ra
        COMPLETED,  // Đã hoàn thành
        CANCELLED   // Đã hủy
    }

    /**
     * Business Rule: Kiểm tra lớp đã đầy chưa
     */
    public boolean isFull() {
        return currentStudents != null && maxStudents != null
               && currentStudents >= maxStudents;
    }

    /**
     * Business Rule: Kiểm tra ngày bắt đầu phải trước ngày kết thúc
     */
    @AssertTrue(message = "Ngày kết thúc phải sau ngày bắt đầu")
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return endDate.isAfter(startDate);
    }
}
