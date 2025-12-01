package com.nute.training.service;

import com.nute.training.entity.Certificate;
import com.nute.training.entity.Enrollment;
import com.nute.training.entity.Grade;
import com.nute.training.entity.User;
import com.nute.training.repository.CertificateRepository;
import com.nute.training.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service: CertificateService
 * Quản lý chứng chỉ
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final GradeRepository gradeRepository;

    /**
     * Tìm tất cả chứng chỉ
     */
    @Transactional(readOnly = true)
    public List<Certificate> findAll() {
        return certificateRepository.findAll();
    }

    /**
     * Tìm chứng chỉ theo ID
     */
    @Transactional(readOnly = true)
    public Optional<Certificate> findById(Long id) {
        return certificateRepository.findById(id);
    }

    /**
     * Tìm chứng chỉ theo mã
     */
    @Transactional(readOnly = true)
    public Optional<Certificate> findByCertificateCode(String code) {
        return certificateRepository.findByCertificateCode(code);
    }

    /**
     * Tìm chứng chỉ theo enrollment
     */
    @Transactional(readOnly = true)
    public Optional<Certificate> findByEnrollment(Enrollment enrollment) {
        return certificateRepository.findByEnrollment(enrollment);
    }

    /**
     * Tìm chứng chỉ của học viên
     */
    @Transactional(readOnly = true)
    public List<Certificate> findCertificatesByStudent(Long studentId) {
        return certificateRepository.findCertificatesByStudent(studentId);
    }

    /**
     * Tìm chứng chỉ theo lớp
     */
    @Transactional(readOnly = true)
    public List<Certificate> findCertificatesByClass(Long classId) {
        return certificateRepository.findCertificatesByClass(classId);
    }

    /**
     * Tìm chứng chỉ đã cấp
     */
    @Transactional(readOnly = true)
    public List<Certificate> findIssuedCertificates() {
        return certificateRepository.findIssuedCertificates();
    }

    /**
     * Tìm enrollment đủ điều kiện nhận chứng chỉ
     * Business Rule:
     * - Enrollment phải ở trạng thái APPROVED
     * - Phải có grade và pass = true (điểm đạt)
     * - Chưa được cấp chứng chỉ
     */
    @Transactional(readOnly = true)
    public List<Enrollment> findEligibleEnrollmentsForCertificate(Long classId) {
        return certificateRepository.findEligibleEnrollmentsForCertificate(classId);
    }

    /**
     * Kiểm tra enrollment có đủ điều kiện nhận chứng chỉ không
     */
    @Transactional(readOnly = true)
    public boolean isEligibleForCertificate(Enrollment enrollment) {
        // Check enrollment status
        if (enrollment.getStatus() != Enrollment.EnrollmentStatus.APPROVED) {
            return false;
        }

        // Check grade exists and passed
        Optional<Grade> grade = gradeRepository.findByEnrollment(enrollment);
        if (grade.isEmpty() || !grade.get().getPass()) {
            return false;
        }

        // Check not already issued
        return !certificateRepository.existsByEnrollment(enrollment);
    }

    /**
     * Tạo chứng chỉ
     * Business Rule:
     * - Certificate code phải unique
     * - Enrollment phải đủ điều kiện (có điểm đạt, chưa cấp chứng chỉ)
     */
    public Certificate createCertificate(Enrollment enrollment,
                                        String certificateCode,
                                        LocalDate issueDate,
                                        User issuedBy,
                                        String notes) {
        log.info("Creating certificate for enrollment ID: {}", enrollment.getId());

        // Validate certificate code unique
        if (certificateRepository.existsByCertificateCode(certificateCode)) {
            throw new IllegalArgumentException(
                    "Mã chứng chỉ đã tồn tại: " + certificateCode);
        }

        // Check eligibility
        if (!isEligibleForCertificate(enrollment)) {
            throw new IllegalStateException(
                    "Học viên chưa đủ điều kiện nhận chứng chỉ (chưa đạt hoặc đã được cấp)");
        }

        // Create certificate
        Certificate certificate = new Certificate();
        certificate.setEnrollment(enrollment);
        certificate.setCertificateCode(certificateCode);
        certificate.setIssueDate(issueDate != null ? issueDate : LocalDate.now());
        certificate.setStatus(Certificate.CertificateStatus.DRAFT);
        certificate.setIssuedBy(issuedBy);
        certificate.setNotes(notes);

        Certificate saved = certificateRepository.save(certificate);
        log.info("Certificate created successfully with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Cấp chứng chỉ (chuyển từ DRAFT sang ISSUED)
     */
    public Certificate issueCertificate(Long certificateId, User issuedBy) {
        log.info("Issuing certificate ID: {} by: {}",
                certificateId, issuedBy.getUsername());

        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy chứng chỉ với ID: " + certificateId));

        if (certificate.getStatus() == Certificate.CertificateStatus.ISSUED) {
            throw new IllegalStateException("Chứng chỉ đã được cấp rồi");
        }

        certificate.setStatus(Certificate.CertificateStatus.ISSUED);
        certificate.setIssuedBy(issuedBy);
        if (certificate.getIssueDate() == null) {
            certificate.setIssueDate(LocalDate.now());
        }

        Certificate updated = certificateRepository.save(certificate);
        log.info("Certificate issued successfully: {}", updated.getCertificateCode());
        return updated;
    }

    /**
     * Cấp chứng chỉ hàng loạt cho lớp
     * Business Rule: Chỉ cấp cho những enrollment đủ điều kiện
     */
    public List<Certificate> issueCertificatesForClass(Long classId,
                                                       User issuedBy,
                                                       String codePrefix) {
        log.info("Issuing certificates for class ID: {}", classId);

        List<Enrollment> eligibleEnrollments =
                findEligibleEnrollmentsForCertificate(classId);

        List<Certificate> certificates = new java.util.ArrayList<>();
        int counter = 1;

        for (Enrollment enrollment : eligibleEnrollments) {
            // Generate unique certificate code
            String code = generateCertificateCode(codePrefix, classId, counter++);

            // Skip if code exists (shouldn't happen but safety check)
            while (certificateRepository.existsByCertificateCode(code)) {
                code = generateCertificateCode(codePrefix, classId, counter++);
            }

            Certificate certificate = new Certificate();
            certificate.setEnrollment(enrollment);
            certificate.setCertificateCode(code);
            certificate.setIssueDate(LocalDate.now());
            certificate.setStatus(Certificate.CertificateStatus.ISSUED);
            certificate.setIssuedBy(issuedBy);

            Certificate saved = certificateRepository.save(certificate);
            certificates.add(saved);
        }

        log.info("Issued {} certificates for class ID: {}", certificates.size(), classId);
        return certificates;
    }

    /**
     * Thu hồi chứng chỉ
     */
    public Certificate revokeCertificate(Long certificateId, String reason) {
        log.info("Revoking certificate ID: {}", certificateId);

        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy chứng chỉ với ID: " + certificateId));

        certificate.setStatus(Certificate.CertificateStatus.REVOKED);
        certificate.setNotes(reason);

        Certificate updated = certificateRepository.save(certificate);
        log.info("Certificate revoked successfully: {}", updated.getCertificateCode());
        return updated;
    }

    /**
     * Cập nhật chứng chỉ
     */
    public Certificate updateCertificate(Long id, Certificate certificateDetails) {
        log.info("Updating certificate ID: {}", id);

        Certificate existing = certificateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy chứng chỉ với ID: " + id));

        // Validate code unique (if changed)
        if (!existing.getCertificateCode().equals(certificateDetails.getCertificateCode())) {
            if (certificateRepository.existsByCertificateCode(
                    certificateDetails.getCertificateCode())) {
                throw new IllegalArgumentException(
                        "Mã chứng chỉ đã tồn tại: " + certificateDetails.getCertificateCode());
            }
            existing.setCertificateCode(certificateDetails.getCertificateCode());
        }

        existing.setIssueDate(certificateDetails.getIssueDate());
        existing.setFilePath(certificateDetails.getFilePath());
        existing.setNotes(certificateDetails.getNotes());

        Certificate updated = certificateRepository.save(existing);
        log.info("Certificate updated successfully: {}", updated.getCertificateCode());
        return updated;
    }

    /**
     * Xóa chứng chỉ
     */
    public void deleteCertificate(Long id) {
        log.info("Deleting certificate ID: {}", id);

        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy chứng chỉ với ID: " + id));

        certificateRepository.delete(certificate);
        log.info("Certificate deleted successfully: {}", certificate.getCertificateCode());
    }

    /**
     * Generate mã chứng chỉ
     * Format: PREFIX-CLASSID-NUMBER (e.g., CERT-2024-CNTT001-001)
     */
    private String generateCertificateCode(String prefix, Long classId, int number) {
        return String.format("%s-%d-%03d",
                prefix != null ? prefix : "CERT",
                classId,
                number);
    }

    /**
     * Đếm số chứng chỉ đã cấp theo lớp
     */
    @Transactional(readOnly = true)
    public Long countIssuedCertificates(Long classId) {
        return certificateRepository.countIssuedCertificatesByClass(classId);
    }
}
