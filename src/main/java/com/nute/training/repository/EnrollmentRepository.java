package com.nute.training.repository;

import com.nute.training.dto.EnrollmentHistoryDto;
import com.nute.training.entity.ClassEntity;
import com.nute.training.entity.Enrollment;
import com.nute.training.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository: EnrollmentRepository
 * Quản lý đăng ký học
 */
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    /**
     * Tìm đăng ký theo học viên và lớp
     */
    Optional<Enrollment> findByStudentAndClassEntity(User student, ClassEntity classEntity);

    /**
     * Kiểm tra học viên đã đăng ký lớp này chưa
     */
    boolean existsByStudentAndClassEntity(User student, ClassEntity classEntity);

    /**
     * Tìm tất cả đăng ký của học viên
     */
    List<Enrollment> findByStudent(User student);

    /**
     * Tìm tất cả đăng ký của lớp
     */
    List<Enrollment> findByClassEntity(ClassEntity classEntity);

    /**
     * Tìm đăng ký theo trạng thái
     */
    List<Enrollment> findByStatus(Enrollment.EnrollmentStatus status);

    /**
     * Tìm đăng ký theo học viên và trạng thái
     */
    List<Enrollment> findByStudentAndStatus(User student, Enrollment.EnrollmentStatus status);

    /**
     * Tìm đăng ký theo lớp và trạng thái
     */
    List<Enrollment> findByClassEntityAndStatus(
            ClassEntity classEntity,
            Enrollment.EnrollmentStatus status
    );

    /**
     * Tìm tất cả đăng ký PENDING (chờ duyệt)
     */
    @Query("SELECT e FROM Enrollment e WHERE e.status = 'PENDING' ORDER BY e.createdAt ASC")
    List<Enrollment> findPendingEnrollments();

    /**
     * Tìm tất cả đăng ký APPROVED của lớp
     * Eager fetch student để tránh lazy loading exception
     */
    @Query("SELECT e FROM Enrollment e " +
           "LEFT JOIN FETCH e.student " +
           "WHERE e.classEntity = :classEntity AND e.status = 'APPROVED' " +
           "ORDER BY e.student.fullName ASC")
    List<Enrollment> findApprovedEnrollmentsByClass(@Param("classEntity") ClassEntity classEntity);

    /**
     * Đếm số học viên đã được duyệt trong lớp
     */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE " +
           "e.classEntity = :classEntity AND e.status = 'APPROVED'")
    Long countApprovedEnrollmentsByClass(@Param("classEntity") ClassEntity classEntity);

    /**
     * Tìm đăng ký theo trạng thái thanh toán
     */
    List<Enrollment> findByPaymentStatus(Enrollment.PaymentStatus paymentStatus);

    /**
     * Tìm đăng ký chưa thanh toán hoặc thanh toán một phần
     */
    @Query("SELECT e FROM Enrollment e WHERE " +
           "e.paymentStatus IN ('UNPAID', 'PARTIAL') AND e.status = 'APPROVED'")
    List<Enrollment> findUnpaidOrPartialEnrollments();

    /**
     * Thống kê đăng ký theo trạng thái
     */
    @Query("SELECT e.status, COUNT(e) FROM Enrollment e GROUP BY e.status")
    List<Object[]> countEnrollmentsByStatus();

    /**
     * Thống kê theo trạng thái thanh toán
     */
    @Query("SELECT e.paymentStatus, COUNT(e) FROM Enrollment e GROUP BY e.paymentStatus")
    List<Object[]> countEnrollmentsByPaymentStatus();

    @Query("SELECT e FROM Enrollment e " +
           "LEFT JOIN FETCH e.student " +
           "LEFT JOIN FETCH e.classEntity ce " +
           "LEFT JOIN FETCH ce.course " +
           "ORDER BY e.createdAt DESC")
    List<Enrollment> findAllWithDetails();

    @Query("SELECT e FROM Enrollment e " +
           "LEFT JOIN FETCH e.student " +
           "LEFT JOIN FETCH e.classEntity ce " +
           "LEFT JOIN FETCH ce.course " +
           "WHERE e.status = :status " +
           "ORDER BY e.createdAt DESC")
    List<Enrollment> findByStatusWithDetails(@Param("status") Enrollment.EnrollmentStatus status);

    /**
     * Tìm enrollment đã duyệt của học viên trong lớp cụ thể
     * (Optimized query - tránh N+1 problem)
     */
    @Query("SELECT e FROM Enrollment e " +
           "WHERE e.student.id = :studentId " +
           "AND e.classEntity.id = :classId " +
           "AND e.status = 'APPROVED'")
    Optional<Enrollment> findApprovedEnrollmentByStudentAndClass(
            @Param("studentId") Long studentId,
            @Param("classId") Long classId
    );

    /**
     * Tìm enrollment history của học viên với DTO projection
     * (Single optimized query - không có N+1 problem)
     */
    @Query("SELECT new com.nute.training.dto.EnrollmentHistoryDto(" +
           "e.id, " +
           "e.enrollmentDate, " +
           "e.status, " +
           "e.paymentStatus, " +
           "e.paymentAmount, " +
           "e.notes, " +
           "e.approvedAt, " +
           "c.id, " +
           "c.classCode, " +
           "c.className, " +
           "c.room, " +
           "c.startDate, " +
           "c.endDate, " +
           "co.id, " +
           "co.name, " +
           "co.code, " +
           "co.tuitionFee, " +
           "t.id, " +
           "t.fullName, " +
           "t.email, " +
           "ab.id, " +
           "ab.fullName" +
           ") " +
           "FROM Enrollment e " +
           "JOIN e.classEntity c " +
           "JOIN c.course co " +
           "LEFT JOIN c.teacher t " +
           "LEFT JOIN e.approvedBy ab " +
           "WHERE e.student = :student " +
           "ORDER BY e.createdAt DESC")
    List<EnrollmentHistoryDto> findEnrollmentHistoryByStudent(@Param("student") User student);
}
