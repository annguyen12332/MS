package com.nute.training.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Entity: StudentInfo
 * Thông tin chi tiết sinh viên (Dùng để cấp chứng chỉ)
 */
@Entity
@Table(name = "student_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "student_code", unique = true, length = 20)
    @NotBlank(message = "Mã sinh viên không được để trống")
    private String studentCode;

    @Column(name = "date_of_birth")
    @NotNull(message = "Ngày sinh không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @Column(name = "place_of_birth")
    @NotBlank(message = "Nơi sinh không được để trống")
    private String placeOfBirth;

    @Column(columnDefinition = "TEXT")
    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    @Column(length = 100)
    @NotBlank(message = "Ngành học không được để trống")
    private String major;

    @Column(name = "specialized_class", length = 50)
    @NotBlank(message = "Lớp chuyên ngành không được để trống")
    private String specializedClass;
}
