package com.nute.training.repository;

import com.nute.training.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository: UserRepository
 * Quản lý người dùng (Admin, Teacher, Student)
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Tìm user theo username
     */
    Optional<User> findByUsername(String username);

    /**
     * Tìm user theo email
     */
    Optional<User> findByEmail(String email);

    /**
     * Kiểm tra username đã tồn tại chưa
     */
    boolean existsByUsername(String username);

    /**
     * Kiểm tra email đã tồn tại chưa
     */
    boolean existsByEmail(String email);

    /**
     * Tìm tất cả user theo role
     */
    List<User> findByRole(User.Role role);

    /**
     * Tìm tất cả user theo role và status
     */
    List<User> findByRoleAndStatus(User.Role role, User.Status status);

    /**
     * Tìm tất cả giảng viên đang hoạt động
     */
    @Query("SELECT u FROM User u WHERE u.role = 'TEACHER' AND u.status = 'ACTIVE'")
    List<User> findActiveTeachers();

    /**
     * Tìm tất cả học viên đang hoạt động
     */
    @Query("SELECT u FROM User u WHERE u.role = 'STUDENT' AND u.status = 'ACTIVE'")
    List<User> findActiveStudents();

    /**
     * Tìm kiếm user theo từ khóa (username, email, fullName)
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchUsers(@Param("keyword") String keyword);

    /**
     * Tìm kiếm user theo role và từ khóa
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND (" +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<User> searchUsersByRoleAndKeyword(@Param("role") User.Role role, @Param("keyword") String keyword);
}
