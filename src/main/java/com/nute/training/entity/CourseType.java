package com.nute.training.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity: CourseType
 * Loại khóa học (CNTT, NN, KNM, SP)
 */
@Entity
@Table(name = "course_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Tên loại khóa học không được để trống")
    private String name;

    @Column(unique = true, nullable = false, length = 20)
    @NotBlank(message = "Mã loại khóa học không được để trống")
    @Size(max = 20, message = "Mã loại khóa học không quá 20 ký tự")
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
