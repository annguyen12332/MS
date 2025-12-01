package com.nute.training.controller.admin;

import com.nute.training.entity.User;
import com.nute.training.service.CertificateService;
import com.nute.training.service.ClassService;
import com.nute.training.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller: AdminCertificateController
 * Cấp chứng chỉ cho học viên đạt yêu cầu
 */
@Controller
@RequestMapping("/admin/certificates")
@RequiredArgsConstructor
@Slf4j
public class AdminCertificateController {

    private final CertificateService certificateService;
    private final ClassService classService;
    private final AuthenticationHelper authenticationHelper;

    /**
     * Danh sách chứng chỉ đã cấp
     */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("certificates", certificateService.findIssuedCertificates());
        return "admin/certificates/list";
    }

    /**
     * Danh sách học viên đủ điều kiện nhận chứng chỉ
     */
    @GetMapping("/class/{classId}/eligible")
    public String eligibleStudents(@PathVariable Long classId, Model model,
                                   RedirectAttributes redirectAttributes) {
        try {
            var classEntity = classService.findById(classId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học"));

            var eligibleEnrollments = certificateService
                    .findEligibleEnrollmentsForCertificate(classId);

            model.addAttribute("classEntity", classEntity);
            model.addAttribute("eligibleEnrollments", eligibleEnrollments);
            return "admin/certificates/eligible";
        } catch (Exception e) {
            log.error("Error loading eligible students", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/classes";
        }
    }

    /**
     * Cấp chứng chỉ hàng loạt cho lớp
     */
    @PostMapping("/issue-batch")
    public String issueBatch(@RequestParam Long classId,
                            @RequestParam(required = false) String codePrefix,
                            RedirectAttributes redirectAttributes) {
        try {
            User currentAdmin = authenticationHelper.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not found"));

            var certificates = certificateService.issueCertificatesForClass(
                    classId,
                    currentAdmin,
                    codePrefix != null ? codePrefix : "CERT"
            );

            redirectAttributes.addFlashAttribute("success",
                    "Cấp chứng chỉ thành công cho " + certificates.size() + " học viên");
        } catch (Exception e) {
            log.error("Error issuing batch certificates", e);
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi khi cấp chứng chỉ: " + e.getMessage());
        }
        return "redirect:/admin/certificates/class/" + classId + "/eligible";
    }

    /**
     * Cấp chứng chỉ đơn lẻ
     */
    @PostMapping("/{id}/issue")
    public String issueSingle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User currentAdmin = authenticationHelper.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not found"));

            var certificate = certificateService.issueCertificate(id, currentAdmin);

            redirectAttributes.addFlashAttribute("success",
                    "Cấp chứng chỉ thành công: " + certificate.getCertificateCode());
        } catch (Exception e) {
            log.error("Error issuing certificate", e);
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi khi cấp chứng chỉ: " + e.getMessage());
        }
        return "redirect:/admin/certificates";
    }

    /**
     * Thu hồi chứng chỉ
     */
    @PostMapping("/{id}/revoke")
    public String revoke(@PathVariable Long id,
                        @RequestParam(required = false) String reason,
                        RedirectAttributes redirectAttributes) {
        try {
            certificateService.revokeCertificate(id, reason);
            redirectAttributes.addFlashAttribute("success", "Thu hồi chứng chỉ thành công");
        } catch (Exception e) {
            log.error("Error revoking certificate", e);
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi khi thu hồi chứng chỉ: " + e.getMessage());
        }
        return "redirect:/admin/certificates/" + id;
    }

    /**
     * Xem chi tiết chứng chỉ
     */
    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model,
                      RedirectAttributes redirectAttributes) {
        try {
            var certificate = certificateService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chứng chỉ"));

            model.addAttribute("certificate", certificate);
            return "admin/certificates/view";
        } catch (Exception e) {
            log.error("Error loading certificate", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/certificates";
        }
    }

    /**
     * Tìm chứng chỉ theo mã
     */
    @GetMapping("/search")
    public String search(@RequestParam String code, Model model,
                        RedirectAttributes redirectAttributes) {
        try {
            var certificate = certificateService.findByCertificateCode(code)
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy chứng chỉ với mã: " + code));

            return "redirect:/admin/certificates/" + certificate.getId();
        } catch (Exception e) {
            log.error("Error searching certificate", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/certificates";
        }
    }
}
