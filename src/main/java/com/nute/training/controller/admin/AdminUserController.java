package com.nute.training.controller.admin;

import com.nute.training.entity.User;
import com.nute.training.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller: AdminUserController
 * Quản lý người dùng (Admin, Teacher, Student)
 */
@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final UserService userService;

    /**
     * Danh sách người dùng
     */
    @GetMapping
    public String list(@RequestParam(required = false) String role,
                      @RequestParam(required = false) String keyword,
                      Model model) {

        if (keyword != null && !keyword.trim().isEmpty()) {
            model.addAttribute("users", userService.searchUsers(keyword));
            model.addAttribute("keyword", keyword);
        } else if (role != null && !role.isEmpty()) {
            User.Role userRole = User.Role.valueOf(role);
            model.addAttribute("users", userService.findByRole(userRole));
            model.addAttribute("selectedRole", role);
        } else {
            model.addAttribute("users", userService.findAll());
        }

        model.addAttribute("roles", User.Role.values());
        model.addAttribute("statuses", User.Status.values());
        return "admin/users/list";
    }

    /**
     * Form tạo người dùng mới
     */
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", User.Role.values());
        model.addAttribute("statuses", User.Status.values());
        return "admin/users/form";
    }

    /**
     * Xử lý tạo người dùng
     */
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute User user,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("roles", User.Role.values());
            model.addAttribute("statuses", User.Status.values());
            return "admin/users/form";
        }

        try {
            userService.createUser(user);
            redirectAttributes.addFlashAttribute("success",
                    "Tạo người dùng thành công: " + user.getFullName());
            return "redirect:/admin/users";
        } catch (Exception e) {
            log.error("Error creating user", e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("roles", User.Role.values());
            model.addAttribute("statuses", User.Status.values());
            return "admin/users/form";
        }
    }

    /**
     * Form chỉnh sửa người dùng
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model,
                          RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            // Don't send password to frontend
            user.setPassword("");

            model.addAttribute("user", user);
            model.addAttribute("roles", User.Role.values());
            model.addAttribute("statuses", User.Status.values());
            return "admin/users/form";
        } catch (Exception e) {
            log.error("Error loading user for edit", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users";
        }
    }

    /**
     * Xử lý cập nhật người dùng
     */
    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute User user,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirectAttributes) {

        // Remove password validation for update
        result = result.hasFieldErrors("password") ? result : result;

        if (result.hasErrors() && !result.hasFieldErrors("password")) {
            model.addAttribute("roles", User.Role.values());
            model.addAttribute("statuses", User.Status.values());
            return "admin/users/form";
        }

        try {
            userService.updateUser(id, user);
            redirectAttributes.addFlashAttribute("success",
                    "Cập nhật người dùng thành công: " + user.getFullName());
            return "redirect:/admin/users";
        } catch (Exception e) {
            log.error("Error updating user", e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("roles", User.Role.values());
            model.addAttribute("statuses", User.Status.values());
            return "admin/users/form";
        }
    }

    /**
     * Thay đổi trạng thái người dùng
     */
    @PostMapping("/{id}/change-status")
    public String changeStatus(@PathVariable Long id,
                              @RequestParam User.Status status,
                              RedirectAttributes redirectAttributes) {
        try {
            userService.changeStatus(id, status);
            redirectAttributes.addFlashAttribute("success",
                    "Thay đổi trạng thái người dùng thành công");
        } catch (Exception e) {
            log.error("Error changing user status", e);
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi khi thay đổi trạng thái: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * Reset mật khẩu
     */
    @PostMapping("/{id}/reset-password")
    public String resetPassword(@PathVariable Long id,
                               @RequestParam String newPassword,
                               RedirectAttributes redirectAttributes) {
        try {
            userService.resetPassword(id, newPassword);
            redirectAttributes.addFlashAttribute("success",
                    "Reset mật khẩu thành công");
        } catch (Exception e) {
            log.error("Error resetting password", e);
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi khi reset mật khẩu: " + e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    /**
     * Xem chi tiết người dùng
     */
    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model,
                      RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            model.addAttribute("user", user);
            return "admin/users/view";
        } catch (Exception e) {
            log.error("Error loading user", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users";
        }
    }

    /**
     * Xóa người dùng
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            String fullName = user.getFullName();
            userService.deleteUser(id);

            redirectAttributes.addFlashAttribute("success",
                    "Xóa người dùng thành công: " + fullName);
        } catch (Exception e) {
            log.error("Error deleting user", e);
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi khi xóa người dùng: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
