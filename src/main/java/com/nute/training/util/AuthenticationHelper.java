package com.nute.training.util;

import com.nute.training.entity.User;
import com.nute.training.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Util: AuthenticationHelper
 * Hỗ trợ lấy thông tin user đang đăng nhập
 */
@Component
@RequiredArgsConstructor
public class AuthenticationHelper {

    private final UserRepository userRepository;

    /**
     * Lấy username của user đang đăng nhập
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }

    /**
     * Lấy User entity của user đang đăng nhập
     */
    public Optional<User> getCurrentUser() {
        String username = getCurrentUsername();
        if (username != null && !"anonymousUser".equals(username)) {
            return userRepository.findByUsername(username);
        }
        return Optional.empty();
    }

    /**
     * Kiểm tra user hiện tại có role cụ thể không
     */
    public boolean hasRole(User.Role role) {
        return getCurrentUser()
                .map(user -> user.getRole() == role)
                .orElse(false);
    }

    /**
     * Kiểm tra user hiện tại có phải ADMIN không
     */
    public boolean isAdmin() {
        return hasRole(User.Role.ADMIN);
    }

    /**
     * Kiểm tra user hiện tại có phải TEACHER không
     */
    public boolean isTeacher() {
        return hasRole(User.Role.TEACHER);
    }

    /**
     * Kiểm tra user hiện tại có phải STUDENT không
     */
    public boolean isStudent() {
        return hasRole(User.Role.STUDENT);
    }
}
