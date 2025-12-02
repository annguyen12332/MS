package com.nute.training.repository;

import com.nute.training.entity.Course;
import com.nute.training.entity.CourseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository: CourseRepository
 * Quản lý khóa học
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    /**
     * Tìm khóa học theo code
     */
    Optional<Course> findByCode(String code);

    /**
     * Kiểm tra code đã tồn tại chưa
     */
    boolean existsByCode(String code);

    /**
     * Tìm tất cả khóa học theo trạng thái
     */
    List<Course> findByStatus(Course.CourseStatus status);

    /**
     * Tìm tất cả khóa học ACTIVE
     */
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.courseType WHERE c.status = :status ORDER BY c.createdAt DESC")
    List<Course> findByStatusOrderByCreatedAtDesc(@Param("status") Course.CourseStatus status);

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.courseType ORDER BY c.createdAt DESC")
    List<Course> findAllWithCourseType();

    /**
     * Tìm khóa học theo loại khóa học
     */
    List<Course> findByCourseType(CourseType courseType);

    /**
     * Tìm khóa học theo loại và trạng thái
     */
    List<Course> findByCourseTypeAndStatus(CourseType courseType, Course.CourseStatus status);

    /**
     * Tìm kiếm khóa học theo từ khóa
     */
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.courseType WHERE " +
           "LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Course> searchCourses(@Param("keyword") String keyword);

    /**
     * Thống kê số lượng khóa học theo trạng thái
     */
    @Query("SELECT c.status, COUNT(c) FROM Course c GROUP BY c.status")
    List<Object[]> countCoursesByStatus();
}
