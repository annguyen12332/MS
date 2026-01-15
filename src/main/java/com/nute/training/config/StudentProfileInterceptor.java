package com.nute.training.config;

import com.nute.training.entity.User;
import com.nute.training.service.StudentInfoService;
import com.nute.training.util.AuthenticationHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor: StudentProfileInterceptor
 * Kiểm tra sinh viên đã cập nhật thông tin chi tiết chưa.
 * Nếu chưa, bắt buộc chuyển hướng đến trang cập nhật hồ sơ.
 */
@Component
@RequiredArgsConstructor
public class StudentProfileInterceptor implements HandlerInterceptor {

    private final StudentInfoService studentInfoService;
    private final AuthenticationHelper authenticationHelper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        
        // Tránh vòng lặp chuyển hướng khi truy cập trang profile
        if (uri.startsWith("/profile") || uri.startsWith("/logout") || uri.startsWith("/css") || uri.startsWith("/js") || uri.startsWith("/assets")) {
            return true;
        }

        authenticationHelper.getCurrentUser().ifPresent(user -> {
            if (user.getRole() == User.Role.STUDENT) {
                if (!studentInfoService.isProfileComplete(user)) {
                    // Nếu chưa hoàn thành hồ sơ, chuyển hướng đến trang chỉnh sửa
                    try {
                        request.getSession().setAttribute("profileIncompleteMessage", "Vui lòng hoàn cập nhật thông tin sinh viên để tiếp tục.");
                        response.sendRedirect("/profile/edit");
                    } catch (Exception e) {
                        // Log error
                    }
                }
            }
        });

        // Nếu profile incomplete, logic trên đã redirect, nhưng preHandle cần return false nếu redirect
        // Để đơn giản và an toàn, ta kiểm tra lại nếu đã commit response
        return !response.isCommitted();
    }
}
