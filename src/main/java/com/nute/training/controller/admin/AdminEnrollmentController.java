package com.nute.training.controller.admin;

import com.nute.training.entity.Enrollment;
import com.nute.training.entity.User;
import com.nute.training.service.ClassService;
import com.nute.training.service.EnrollmentService;
import com.nute.training.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller: AdminEnrollmentController
 * Quản lý đăng ký học - Duyệt/Từ chối đăng ký
 */
@Controller
@RequestMapping("/admin/enrollments")
@RequiredArgsConstructor
@Slf4j
public class AdminEnrollmentController {

    private final EnrollmentService enrollmentService;
    private final ClassService classService;
    private final AuthenticationHelper authenticationHelper;

    /**
     * Danh sách tất cả đăng ký
     */
    @GetMapping
    public String list(@RequestParam(required = false) String status, Model model) {
        if (status != null && !status.isEmpty()) {
            Enrollment.EnrollmentStatus enrollmentStatus = Enrollment.EnrollmentStatus.valueOf(status);
            model.addAttribute("enrollments", enrollmentService.findByStatus(enrollmentStatus));
            model.addAttribute("selectedStatus", status);
        } else {
            model.addAttribute("enrollments", enrollmentService.findAll());
        }
        model.addAttribute("statuses", Enrollment.EnrollmentStatus.values());
        return "admin/enrollments/list";
    }

    /**
     * Danh sách đăng ký chờ duyệt (PENDING)
     */
    @GetMapping("/pending")
    public String pendingList(Model model) {
        model.addAttribute("enrollments", enrollmentService.findPendingEnrollments());
        model.addAttribute("pageTitle", "Đăng ký chờ duyệt");
        return "admin/enrollments/pending";
    }

    /**
     * Xem chi tiết đăng ký
     */
    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Enrollment enrollment = enrollmentService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đăng ký"));

            model.addAttribute("enrollment", enrollment);
            return "admin/enrollments/view";
        } catch (Exception e) {
            log.error("Error loading enrollment", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/enrollments";
        }
    }

    /**
     * Duyệt đăng ký
     * Business Rule:
     * - Chỉ duyệt đăng ký PENDING
     * - Lớp phải chưa đầy
     * - Tăng currentStudents của lớp
     */
    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User currentAdmin = authenticationHelper.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Enrollment enrollment = enrollmentService.approveEnrollment(id, currentAdmin);

            redirectAttributes.addFlashAttribute("success",
                    "Duyệt đăng ký thành công cho học viên: " +
                    enrollment.getStudent().getFullName());
        } catch (Exception e) {
            log.error("Error approving enrollment", e);
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi khi duyệt đăng ký: " + e.getMessage());
        }
        return "redirect:/admin/enrollments/pending";
    }

    /**
     * Từ chối đăng ký
     */
    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id,
                        @RequestParam(required = false) String reason,
                        RedirectAttributes redirectAttributes) {
        try {
            User currentAdmin = authenticationHelper.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Enrollment enrollment = enrollmentService.rejectEnrollment(id, currentAdmin, reason);

            redirectAttributes.addFlashAttribute("success",
                    "Từ chối đăng ký cho học viên: " +
                    enrollment.getStudent().getFullName());
        } catch (Exception e) {
            log.error("Error rejecting enrollment", e);
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi khi từ chối đăng ký: " + e.getMessage());
        }
        return "redirect:/admin/enrollments/pending";
    }

    /**
     * Hủy đăng ký
     */
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            enrollmentService.cancelEnrollment(id);
            redirectAttributes.addFlashAttribute("success", "Hủy đăng ký thành công");
        } catch (Exception e) {
            log.error("Error cancelling enrollment", e);
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi khi hủy đăng ký: " + e.getMessage());
        }
        return "redirect:/admin/enrollments";
    }

    /**
     * Cập nhật thanh toán
     */
    @PostMapping("/{id}/update-payment")
    public String updatePayment(@PathVariable Long id,
                               @RequestParam java.math.BigDecimal amount,
                               RedirectAttributes redirectAttributes) {
        try {
            enrollmentService.updatePayment(id, amount);
            redirectAttributes.addFlashAttribute("success",
                    "Cập nhật thanh toán thành công");
        } catch (Exception e) {
            log.error("Error updating payment", e);
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi khi cập nhật thanh toán: " + e.getMessage());
        }
        return "redirect:/admin/enrollments/" + id;
    }

    /**
     * Danh sách đăng ký theo lớp
     */
    @GetMapping("/class/{classId}")
    public String listByClass(@PathVariable Long classId, Model model,
                             RedirectAttributes redirectAttributes) {
        try {
            var classEntity = classService.findById(classId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học"));

            model.addAttribute("classEntity", classEntity);
            model.addAttribute("enrollments", enrollmentService.findByClass(classEntity));
            model.addAttribute("approvedCount",
                    enrollmentService.countApprovedEnrollmentsByClass(classEntity));
            return "admin/enrollments/list-by-class";
        } catch (Exception e) {
            log.error("Error loading enrollments by class", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/classes";
        }
    }
}
