package com.nute.training.controller.common;

import com.nute.training.entity.User;
import com.nute.training.service.UserService;
import com.nute.training.util.AuthenticationHelper;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller: ProfileController
 * Quản lý hồ sơ cá nhân cho mọi role
 */
@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final UserService userService;
    private final com.nute.training.service.StudentInfoService studentInfoService;
    private final AuthenticationHelper authenticationHelper;

    /**
     * Helper method to get layout path based on user role
     */
    private String getLayoutPath(User.Role role) {
        return switch (role) {
            case ADMIN -> "admin/layout";
            case TEACHER -> "teacher/layout";
            case STUDENT -> "student/layout";
        };
    }

    /**
     * Xem hồ sơ cá nhân
     */
    @GetMapping
    public String viewProfile(Model model) {
        User currentUser = authenticationHelper.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", currentUser);
        
        if (currentUser.getRole() == User.Role.STUDENT) {
            model.addAttribute("studentInfo", studentInfoService.findByUserId(currentUser.getId()).orElse(null));
        }
        
        model.addAttribute("layoutPath", getLayoutPath(currentUser.getRole()));
        return "profile/view";
    }

    /**
     * Form chỉnh sửa hồ sơ cá nhân
     */
    @GetMapping("/edit")
    public String editProfileForm(Model model) {
        User currentUser = authenticationHelper.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", currentUser);
        
        if (currentUser.getRole() == User.Role.STUDENT) {
            model.addAttribute("studentInfo", studentInfoService.findByUserId(currentUser.getId()).orElse(new com.nute.training.entity.StudentInfo()));
        }
        
        model.addAttribute("layoutPath", getLayoutPath(currentUser.getRole()));
        return "profile/edit";
    }

    /**
     * Xử lý cập nhật hồ sơ cá nhân và thông tin sinh viên
     */
    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute("user") User userDetails,
                                BindingResult result,
                                @ModelAttribute("studentInfo") com.nute.training.entity.StudentInfo studentInfo,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        User currentUser = authenticationHelper.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!currentUser.getId().equals(userDetails.getId())) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa hồ sơ của người khác.");
        }

        if (result.hasErrors()) {
            // Check if only password error (which is expected as it's not in the form)
            if (!(result.getErrorCount() == 1 && result.hasFieldErrors("password"))) {
                model.addAttribute("layoutPath", getLayoutPath(currentUser.getRole()));
                return "profile/edit";
            }
        }

        try {
            // Update basic info
            userService.updateUserProfile(currentUser.getId(), userDetails);
            
            // Update student info if role is STUDENT
            if (currentUser.getRole() == User.Role.STUDENT && studentInfo != null) {
                studentInfoService.saveOrUpdate(currentUser.getId(), studentInfo);
            }
            
            redirectAttributes.addFlashAttribute("success", "Cập nhật hồ sơ thành công!");
            return "redirect:/profile";
        } catch (Exception e) {
            log.error("Error updating profile for user {}: {}", currentUser.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật hồ sơ: " + e.getMessage());
            return "redirect:/profile/edit";
        }
    }

    /**
     * Form đổi mật khẩu
     */
    @GetMapping("/change-password")
    public String changePasswordForm(Model model) {
        User currentUser = authenticationHelper.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("layoutPath", getLayoutPath(currentUser.getRole()));
        model.addAttribute("currentPassword", "");
        model.addAttribute("newPassword", "");
        model.addAttribute("confirmPassword", "");
        return "profile/change-password";
    }

    /**
     * Xử lý đổi mật khẩu
     */
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = authenticationHelper.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới và xác nhận mật khẩu không khớp.");
            return "redirect:/profile/change-password";
        }

        try {
            userService.changePassword(currentUser.getId(), currentPassword, newPassword); // Need to create this method in UserService
            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
            return "redirect:/profile";
        } catch (Exception e) {
            log.error("Error changing password for user {}: {}", currentUser.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Lỗi khi đổi mật khẩu: " + e.getMessage());
            return "redirect:/profile/change-password";
        }
    }

    @PostMapping("/clear-incomplete-message")
    @ResponseBody
    public String clearIncompleteMessage(HttpServletRequest request) {
        request.getSession().removeAttribute("profileIncompleteMessage");
        return "ok";
    }
}
