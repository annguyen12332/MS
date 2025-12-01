package com.nute.training.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity: Attendance
 * Điểm danh
 */
@Entity
@Table(name = "attendances",
       uniqueConstraints = @UniqueConstraint(columnNames = {"schedule_id", "student_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    @NotNull(message = "Buổi học không được để trống")
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @NotNull(message = "Học viên không được để trống")
    private User student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Trạng thái điểm danh không được để trống")
    private AttendanceStatus status;

    @Column(columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marked_by")
    private User markedBy;

    @Column(name = "marked_at")
    private LocalDateTime markedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Enum: AttendanceStatus
     * Trạng thái điểm danh
     */
    public enum AttendanceStatus {
        PRESENT,    // Có mặt
        ABSENT,     // Vắng
        LATE,       // Đi muộn
        EXCUSED     // Có phép
    }

    /**
     * Business Rule: Kiểm tra có được tính điểm chuyên cần không
     */
    public boolean isCountedForAttendanceScore() {
        return status == AttendanceStatus.PRESENT || status == AttendanceStatus.LATE;
    }
}
