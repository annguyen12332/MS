package com.nute.training.repository;

import com.nute.training.entity.Enrollment;
import com.nute.training.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository: GradeRepository
 * Quản lý điểm số
 */
@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    /**
     * Tìm điểm theo enrollment
     */
    Optional<Grade> findByEnrollment(Enrollment enrollment);

    /**
     * Kiểm tra enrollment đã có điểm chưa
     */
    boolean existsByEnrollment(Enrollment enrollment);

    /**
     * Tìm điểm theo học viên (thông qua enrollment)
     */
    @Query("SELECT DISTINCT g FROM Grade g " +
           "LEFT JOIN FETCH g.enrollment e " +
           "LEFT JOIN FETCH e.classEntity c " +
           "LEFT JOIN FETCH c.course " +
           "LEFT JOIN FETCH c.teacher " +
           "WHERE e.student.id = :studentId " +
           "ORDER BY c.className ASC, g.createdAt DESC")
    List<Grade> findGradesByStudent(@Param("studentId") Long studentId);

    /**
     * Tìm điểm theo lớp
     */
    @Query("SELECT g FROM Grade g " +
           "LEFT JOIN FETCH g.enrollment e " +
           "LEFT JOIN FETCH e.student " +
           "WHERE g.enrollment.classEntity.id = :classId " +
           "ORDER BY e.student.fullName ASC")
    List<Grade> findGradesByClass(@Param("classId") Long classId);

    /**
     * Tìm điểm của học viên trong lớp
     */
    @Query("SELECT g FROM Grade g WHERE " +
           "g.enrollment.student.id = :studentId AND g.enrollment.classEntity.id = :classId")
    Optional<Grade> findGradeByStudentAndClass(
            @Param("studentId") Long studentId,
            @Param("classId") Long classId
    );

    /**
     * Tìm học viên đạt trong lớp
     */
    @Query("SELECT g FROM Grade g WHERE " +
           "g.enrollment.classEntity.id = :classId AND g.pass = true")
    List<Grade> findPassedGradesByClass(@Param("classId") Long classId);

    /**
     * Tìm học viên không đạt trong lớp
     */
    @Query("SELECT g FROM Grade g WHERE " +
           "g.enrollment.classEntity.id = :classId AND g.pass = false")
    List<Grade> findFailedGradesByClass(@Param("classId") Long classId);

    /**
     * Đếm số học viên đạt trong lớp
     */
    @Query("SELECT COUNT(g) FROM Grade g WHERE " +
           "g.enrollment.classEntity.id = :classId AND g.pass = true")
    Long countPassedGradesByClass(@Param("classId") Long classId);

    /**
     * Đếm số học viên không đạt trong lớp
     */
    @Query("SELECT COUNT(g) FROM Grade g WHERE " +
           "g.enrollment.classEntity.id = :classId AND g.pass = false")
    Long countFailedGradesByClass(@Param("classId") Long classId);

    /**
     * Tính điểm trung bình của lớp
     */
    @Query("SELECT AVG(g.totalScore) FROM Grade g WHERE " +
           "g.enrollment.classEntity.id = :classId AND g.totalScore IS NOT NULL")
    Double calculateAverageScoreByClass(@Param("classId") Long classId);

    /**
     * Thống kê theo xếp loại
     */
    @Query("SELECT g.gradeLetter, COUNT(g) FROM Grade g WHERE " +
           "g.enrollment.classEntity.id = :classId GROUP BY g.gradeLetter")
    List<Object[]> countGradesByLetterInClass(@Param("classId") Long classId);

    /**
     * Tìm top học viên có điểm cao nhất trong lớp
     */
    @Query("SELECT g FROM Grade g WHERE g.enrollment.classEntity.id = :classId " +
           "AND g.totalScore IS NOT NULL ORDER BY g.totalScore DESC")
    List<Grade> findTopGradesByClass(@Param("classId") Long classId);

    /**
     * Tính tỷ lệ đạt (%) của lớp
     */
    @Query("SELECT " +
           "CAST(COUNT(CASE WHEN g.pass = true THEN 1 END) AS double) / " +
           "CAST(COUNT(g) AS double) * 100 " +
           "FROM Grade g WHERE g.enrollment.classEntity.id = :classId")
    Double calculatePassRateByClass(@Param("classId") Long classId);
}
