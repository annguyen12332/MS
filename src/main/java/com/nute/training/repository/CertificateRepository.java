package com.nute.training.repository;

import com.nute.training.entity.Certificate;
import com.nute.training.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository: CertificateRepository
 * Quản lý chứng chỉ
 */
@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    /**
     * Tìm chứng chỉ theo mã
     */
    Optional<Certificate> findByCertificateCode(String certificateCode);

    /**
     * Kiểm tra mã chứng chỉ đã tồn tại chưa
     */
    boolean existsByCertificateCode(String certificateCode);

    /**
     * Tìm chứng chỉ theo enrollment
     */
    Optional<Certificate> findByEnrollment(Enrollment enrollment);

    /**
     * Kiểm tra enrollment đã có chứng chỉ chưa
     */
    boolean existsByEnrollment(Enrollment enrollment);

    /**
     * Tìm chứng chỉ theo học viên
     */
    @Query("SELECT c FROM Certificate c WHERE c.enrollment.student.id = :studentId")
    List<Certificate> findCertificatesByStudent(@Param("studentId") Long studentId);

    /**
     * Tìm chứng chỉ theo lớp
     */
    @Query("SELECT c FROM Certificate c WHERE c.enrollment.classEntity.id = :classId")
    List<Certificate> findCertificatesByClass(@Param("classId") Long classId);

    /**
     * Tìm chứng chỉ theo khóa học
     */
    @Query("SELECT c FROM Certificate c WHERE c.enrollment.classEntity.course.id = :courseId")
    List<Certificate> findCertificatesByCourse(@Param("courseId") Long courseId);

    /**
     * Tìm chứng chỉ theo trạng thái
     */
    List<Certificate> findByStatus(Certificate.CertificateStatus status);

    /**
     * Tìm chứng chỉ đã cấp
     */
    @Query("SELECT c FROM Certificate c " +
           "JOIN FETCH c.enrollment e " +
           "JOIN FETCH e.student " +
           "JOIN FETCH e.classEntity ce " +
           "JOIN FETCH ce.course " +
           "WHERE c.status = 'ISSUED' ORDER BY c.issueDate DESC")
    List<Certificate> findIssuedCertificates();

    /**
     * Tìm chứng chỉ đã cấp của học viên
     */
    @Query("SELECT c FROM Certificate c WHERE " +
           "c.enrollment.student.id = :studentId AND c.status = 'ISSUED' " +
           "ORDER BY c.issueDate DESC")
    List<Certificate> findIssuedCertificatesByStudent(@Param("studentId") Long studentId);

    /**
     * Đếm số chứng chỉ đã cấp theo lớp
     */
    @Query("SELECT COUNT(c) FROM Certificate c WHERE " +
           "c.enrollment.classEntity.id = :classId AND c.status = 'ISSUED'")
    Long countIssuedCertificatesByClass(@Param("classId") Long classId);

    /**
     * Đếm số chứng chỉ đã cấp theo khóa học
     */
    @Query("SELECT COUNT(c) FROM Certificate c WHERE " +
           "c.enrollment.classEntity.course.id = :courseId AND c.status = 'ISSUED'")
    Long countIssuedCertificatesByCourse(@Param("courseId") Long courseId);

    /**
     * Thống kê chứng chỉ theo trạng thái
     */
    @Query("SELECT c.status, COUNT(c) FROM Certificate c GROUP BY c.status")
    List<Object[]> countCertificatesByStatus();

    /**
     * Tìm enrollment đủ điều kiện nhận chứng chỉ (đã đạt, chưa có chứng chỉ)
     */
    @Query("SELECT e FROM Enrollment e WHERE " +
           "e.classEntity.id = :classId AND e.status = 'APPROVED' AND " +
           "EXISTS (SELECT g FROM Grade g WHERE g.enrollment = e AND g.pass = true) AND " +
           "NOT EXISTS (SELECT c FROM Certificate c WHERE c.enrollment = e)")
    List<Enrollment> findEligibleEnrollmentsForCertificate(@Param("classId") Long classId);
}
