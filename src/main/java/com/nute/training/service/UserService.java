package com.nute.training.service;

import com.nute.training.entity.User;
import com.nute.training.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service: UserService
 * Quản lý người dùng (Admin, Teacher, Student)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Tìm tất cả users
     */
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Tìm user theo ID
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Tìm user theo ID, bao gồm StudentInfo nếu user là học viên
     */
    @Transactional(readOnly = true)
    public Optional<User> findUserDetailById(Long id) {
        return userRepository.findByIdWithStudentInfo(id);
    }

    /**
     * Tìm user theo username
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Tìm user theo email
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Tìm users theo role
     */
    @Transactional(readOnly = true)
    public List<User> findByRole(User.Role role) {
        return userRepository.findByRole(role);
    }

    /**
     * Tìm giảng viên đang hoạt động
     */
    @Transactional(readOnly = true)
    public List<User> findActiveTeachers() {
        return userRepository.findActiveTeachers();
    }

    /**
     * Tìm học viên đang hoạt động
     */
    @Transactional(readOnly = true)
    public List<User> findActiveStudents() {
        return userRepository.findActiveStudents();
    }

    /**
     * Tìm kiếm users theo từ khóa
     */
    @Transactional(readOnly = true)
    public List<User> searchUsers(String keyword) {
        return userRepository.searchUsers(keyword);
    }

    /**
     * Tìm kiếm users theo role và từ khóa
     */
    @Transactional(readOnly = true)
    public List<User> searchUsersByRoleAndKeyword(User.Role role, String keyword) {
        return userRepository.searchUsersByRoleAndKeyword(role, keyword);
    }

    /**
     * Tạo user mới
     * Business Rule:
     * - Username và email phải unique
     * - Password phải được mã hóa BCrypt
     */
    public User createUser(User user) {
        log.info("Creating new user: {}", user.getUsername());

        // Validate username unique
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException(
                    "Tên đăng nhập đã tồn tại: " + user.getUsername());
        }

        // Validate email unique
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException(
                    "Email đã tồn tại: " + user.getEmail());
        }

        // Encrypt password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Set default status
        if (user.getStatus() == null) {
            user.setStatus(User.Status.ACTIVE);
        }

        User savedUser = userRepository.save(user);
        log.info("User created successfully: {}", savedUser.getUsername());
        return savedUser;
    }

    /**
     * Cập nhật user
     * Business Rule:
     * - Không được thay đổi username
     * - Email phải unique (trừ email hiện tại)
     */
    public User updateUser(Long id, User userDetails) {
        log.info("Updating user: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy user với ID: " + id));

        // Validate email unique (nếu thay đổi email)
        if (!existingUser.getEmail().equals(userDetails.getEmail())) {
            if (userRepository.existsByEmail(userDetails.getEmail())) {
                throw new IllegalArgumentException(
                        "Email đã tồn tại: " + userDetails.getEmail());
            }
            existingUser.setEmail(userDetails.getEmail());
        }

        // Update fields
        existingUser.setFullName(userDetails.getFullName());
        existingUser.setPhone(userDetails.getPhone());
        existingUser.setRole(userDetails.getRole()); // Admin can update role
        existingUser.setStatus(userDetails.getStatus()); // Admin can update status

        if (userDetails.getAvatar() != null) {
            existingUser.setAvatar(userDetails.getAvatar());
        }

        User updatedUser = userRepository.save(existingUser);
        log.info("User updated successfully: {}", updatedUser.getUsername());
        return updatedUser;
    }

    /**
     * Cập nhật hồ sơ cá nhân (người dùng tự cập nhật)
     * Business Rule:
     * - Chỉ cập nhật fullName, email, phone, avatar
     * - Email phải unique (trừ email hiện tại)
     */
    public User updateUserProfile(Long id, User userDetails) {
        log.info("Updating user profile for user ID: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy user với ID: " + id));

        // Validate email unique (nếu thay đổi email)
        if (!existingUser.getEmail().equals(userDetails.getEmail())) {
            if (userRepository.existsByEmail(userDetails.getEmail())) {
                throw new IllegalArgumentException(
                        "Email đã tồn tại: " + userDetails.getEmail());
            }
            existingUser.setEmail(userDetails.getEmail());
        }

        // Update only permitted fields
        existingUser.setFullName(userDetails.getFullName());
        existingUser.setPhone(userDetails.getPhone());
        if (userDetails.getAvatar() != null) {
            existingUser.setAvatar(userDetails.getAvatar());
        }

        User updatedUser = userRepository.save(existingUser);
        log.info("User profile updated successfully for user: {}", updatedUser.getUsername());
        return updatedUser;
    }

    /**
     * Đổi mật khẩu
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("Changing password for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy user với ID: " + userId));

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không đúng");
        }

        // Validate new password
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException(
                    "Mật khẩu mới phải có ít nhất 6 ký tự");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", user.getUsername());
    }

    /**
     * Reset mật khẩu (Admin only)
     */
    public void resetPassword(Long userId, String newPassword) {
        log.info("Resetting password for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy user với ID: " + userId));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password reset successfully for user: {}", user.getUsername());
    }

    /**
     * Thay đổi trạng thái user
     */
    public void changeStatus(Long userId, User.Status status) {
        log.info("Changing status for user ID: {} to {}", userId, status);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy user với ID: " + userId));

        user.setStatus(status);
        userRepository.save(user);

        log.info("Status changed successfully for user: {}", user.getUsername());
    }

    /**
     * Xóa user
     * Business Rule: Chỉ xóa nếu user không có dữ liệu liên quan
     */
    public void deleteUser(Long id) {
        log.info("Deleting user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy user với ID: " + id));

        // TODO: Check if user has related data (enrollments, classes, etc.)
        // For now, just delete

        userRepository.delete(user);
        log.info("User deleted successfully: {}", user.getUsername());
    }

    /**
     * Kiểm tra username đã tồn tại chưa
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Kiểm tra email đã tồn tại chưa
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
