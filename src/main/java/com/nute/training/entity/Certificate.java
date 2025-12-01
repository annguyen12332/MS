package com.nute.training.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity: Certificate
 * Chứng chỉ
 */
@Entity
@Table(name = "certificates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    @NotNull(message = "Đăng ký học không được để trống")
    private Enrollment enrollment;

    @Column(name = "certificate_code", unique = true, nullable = false, length = 50)
    @NotBlank(message = "Mã chứng chỉ không được để trống")
    @Size(max = 50, message = "Mã chứng chỉ không quá 50 ký tự")
    private String certificateCode;

    @Column(name = "issue_date", nullable = false)
    @NotNull(message = "Ngày cấp không được để trống")
    private LocalDate issueDate;

    @Column(name = "file_path")
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CertificateStatus status = CertificateStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_by")
    private User issuedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Enum: CertificateStatus
     * Trạng thái chứng chỉ
     */
    public enum CertificateStatus {
        DRAFT,      // Nháp
        ISSUED,     // Đã cấp
        REVOKED     // Đã thu hồi
    }
}
