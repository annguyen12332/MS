package com.nute.training.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity: Schedule
 * Thời khóa biểu
 */
@Entity
@Table(name = "schedules",
       uniqueConstraints = @UniqueConstraint(columnNames = {"class_id", "session_number"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    @NotNull(message = "Lớp học không được để trống")
    private ClassEntity classEntity;

    @Column(name = "session_number", nullable = false)
    @NotNull(message = "Buổi học không được để trống")
    @Min(value = 1, message = "Buổi học phải >= 1")
    private Integer sessionNumber;

    @Column(name = "session_date", nullable = false)
    @NotNull(message = "Ngày học không được để trống")
    private LocalDate sessionDate;

    @Column(name = "start_time", nullable = false)
    @NotNull(message = "Giờ bắt đầu không được để trống")
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    @NotNull(message = "Giờ kết thúc không được để trống")
    private LocalTime endTime;

    @Column(length = 50)
    private String room;

    @Column(length = 200)
    private String topic;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleStatus status = ScheduleStatus.SCHEDULED;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Enum: ScheduleStatus
     * Trạng thái buổi học
     */
    public enum ScheduleStatus {
        SCHEDULED,  // Đã lên lịch
        COMPLETED,  // Đã hoàn thành
        CANCELLED   // Đã hủy
    }

    /**
     * Business Rule: Kiểm tra giờ kết thúc phải sau giờ bắt đầu
     */
    @AssertTrue(message = "Giờ kết thúc phải sau giờ bắt đầu")
    public boolean isValidTimeRange() {
        if (startTime == null || endTime == null) {
            return true;
        }
        return endTime.isAfter(startTime);
    }
}
