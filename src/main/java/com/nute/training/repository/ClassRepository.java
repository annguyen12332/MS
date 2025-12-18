package com.nute.training.repository;

import com.nute.training.entity.ClassEntity;
import com.nute.training.entity.Course;
import com.nute.training.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository: ClassRepository
 * Quản lý lớp học
 */
@Repository
public interface ClassRepository extends JpaRepository<ClassEntity, Long> {

    /**
     * Tìm lớp theo class code
     */
    Optional<ClassEntity> findByClassCode(String classCode);

    /**
     * Kiểm tra class code đã tồn tại chưa
     */
    boolean existsByClassCode(String classCode);

    /**
     * Tìm tất cả lớp theo khóa học
     */
    List<ClassEntity> findByCourse(Course course);

    /**
     * Tìm tất cả lớp theo giảng viên
     */
    List<ClassEntity> findByTeacher(User teacher);

    /**
     * Tìm tất cả lớp theo trạng thái
     */
    List<ClassEntity> findByStatus(ClassEntity.ClassStatus status);

    /**
     * Tìm tất cả lớp theo giảng viên và trạng thái
     */
    List<ClassEntity> findByTeacherAndStatus(User teacher, ClassEntity.ClassStatus status);

    /**
     * Tìm tất cả lớp đang diễn ra của giảng viên
     */
    @Query("SELECT c FROM ClassEntity c WHERE c.teacher = :teacher AND c.status = 'ONGOING'")
    List<ClassEntity> findOngoingClassesByTeacher(@Param("teacher") User teacher);

    /**
     * Tìm lớp theo khoảng thời gian
     */
    @Query("SELECT c FROM ClassEntity c WHERE " +
           "c.startDate <= :endDate AND c.endDate >= :startDate")
    List<ClassEntity> findClassesByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Tìm kiếm lớp theo từ khóa
     */
    @Query("SELECT c FROM ClassEntity c WHERE " +
           "LOWER(c.classCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.className) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.course.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<ClassEntity> searchClasses(@Param("keyword") String keyword);

    /**
     * Tìm kiếm lớp theo trạng thái và từ khóa
     */
    @Query("SELECT c FROM ClassEntity c WHERE c.status = :status AND (" +
           "LOWER(c.classCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.className) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.course.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<ClassEntity> searchClassesByStatusAndKeyword(@Param("status") ClassEntity.ClassStatus status, @Param("keyword") String keyword);

    /**
     * Thống kê số lượng lớp theo trạng thái
     */
    @Query("SELECT c.status, COUNT(c) FROM ClassEntity c GROUP BY c.status")
    List<Object[]> countClassesByStatus();

    /**
     * Tìm lớp chưa đầy (còn chỗ)
     * Cho phép đăng ký lớp PENDING (chưa bắt đầu) và ONGOING (đang diễn ra)
     * Eager fetch course và teacher để tránh N+1 problem
     */
    @Query("SELECT DISTINCT c FROM ClassEntity c " +
           "LEFT JOIN FETCH c.course " +
           "LEFT JOIN FETCH c.teacher " +
           "WHERE (c.status = 'PENDING' OR c.status = 'ONGOING') " +
           "AND c.currentStudents < c.maxStudents " +
           "ORDER BY c.startDate ASC")
    List<ClassEntity> findAvailableClasses();

    /**
     * Tìm lớp đang mở đăng ký theo khóa học
     * Cho phép đăng ký lớp PENDING (chưa bắt đầu) và ONGOING (đang diễn ra)
     * Eager fetch course và teacher để tránh N+1 problem
     */
    @Query("SELECT DISTINCT c FROM ClassEntity c " +
           "LEFT JOIN FETCH c.course " +
           "LEFT JOIN FETCH c.teacher " +
           "WHERE c.course = :course " +
           "AND (c.status = 'PENDING' OR c.status = 'ONGOING') " +
           "AND c.currentStudents < c.maxStudents " +
           "ORDER BY c.startDate ASC")
    List<ClassEntity> findOpenClassesByCourse(@Param("course") Course course);
}
