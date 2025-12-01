package com.nute.training.controller.teacher;

import com.nute.training.entity.ClassEntity;
import com.nute.training.entity.Enrollment;
import com.nute.training.entity.Grade;
import com.nute.training.entity.User;
import com.nute.training.service.ClassService;
import com.nute.training.service.EnrollmentService;
import com.nute.training.service.GradeService;
import com.nute.training.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Controller: TeacherGradeController
 * Giảng viên nhập điểm cho học viên
 */
@Controller
@RequestMapping("/teacher/grades")
@RequiredArgsConstructor
@Slf4j
public class TeacherGradeController {

    private final ClassService classService;
    private final EnrollmentService enrollmentService;
    private final GradeService gradeService;
    private final AuthenticationHelper authenticationHelper;

    /**
     * Danh sách lớp học để nhập điểm
     */
    @GetMapping
    public String listClasses(Model model) {
        User currentTeacher = authenticationHelper.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Lấy danh sách lớp (bao gồm cả lớp đã kết thúc để xem/sửa điểm)
        // TODO: Có thể cần method findClassesByTeacher (all status)
        var classes = classService.findOngoingClassesByTeacher(currentTeacher);
        model.addAttribute("classes", classes);
        return "teacher/grades/classes";
    }

    /**
     * Danh sách bảng điểm của lớp
     */
    @GetMapping("/class/{classId}")
    public String classGrades(@PathVariable Long classId, Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            User currentTeacher = authenticationHelper.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ClassEntity classEntity = classService.findById(classId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học"));

            // Verify teacher
            if (classEntity.getTeacher() == null ||
                !classEntity.getTeacher().getId().equals(currentTeacher.getId())) {
                throw new RuntimeException("Bạn không phải giảng viên của lớp này");
            }

            // Lấy danh sách học viên đã được duyệt
            List<Enrollment> enrollments = enrollmentService.findApprovedEnrollmentsByClass(classEntity);

            // Lấy danh sách điểm hiện tại
            List<Grade> grades = gradeService.findGradesByClass(classId);

            model.addAttribute("classEntity", classEntity);
            model.addAttribute("enrollments", enrollments);
            model.addAttribute("grades", grades);
            
            return "teacher/grades/list";
        } catch (Exception e) {
            log.error("Error loading class grades", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/teacher/grades";
        }
    }

    /**
     * Form nhập/sửa điểm cho một học viên
     */
    @GetMapping("/class/{classId}/student/{studentId}")
    public String editGradeForm(@PathVariable Long classId,
                                @PathVariable Long studentId,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            User currentTeacher = authenticationHelper.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ClassEntity classEntity = classService.findById(classId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học"));

            // Verify teacher
            if (classEntity.getTeacher() == null ||
                !classEntity.getTeacher().getId().equals(currentTeacher.getId())) {
                throw new RuntimeException("Bạn không phải giảng viên của lớp này");
            }

            // Tìm enrollment của học viên trong lớp này
            // (Cần tìm enrollment trước để đảm bảo học viên có trong lớp)
            // Có thể dùng GradeService.findGradeByStudentAndClass hoặc EnrollmentService
            // Ở đây ta tìm Enrollment trước
            // TODO: EnrollmentService cần method tìm theo student và class chính xác
            // Tạm thời dùng enrollmentService.findByClass và filter
            Optional<Enrollment> enrollmentOpt = enrollmentService.findApprovedEnrollmentsByClass(classEntity)
                    .stream()
                    .filter(e -> e.getStudent().getId().equals(studentId))
                    .findFirst();

            if (enrollmentOpt.isEmpty()) {
                 throw new RuntimeException("Học viên không thuộc lớp này hoặc chưa được duyệt");
            }

            Enrollment enrollment = enrollmentOpt.get();
            Optional<Grade> gradeOpt = gradeService.findByEnrollment(enrollment);
            Grade grade = gradeOpt.orElse(new Grade());

            model.addAttribute("classEntity", classEntity);
            model.addAttribute("enrollment", enrollment);
            model.addAttribute("grade", grade);
            model.addAttribute("student", enrollment.getStudent());

            return "teacher/grades/form";
        } catch (Exception e) {
            log.error("Error loading grade form", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/teacher/grades/class/" + classId;
        }
    }

    /**
     * Xử lý lưu điểm
     */
    @PostMapping("/save")
    public String saveGrade(@RequestParam Long enrollmentId,
                            @RequestParam(required = false) BigDecimal attendanceScore,
                            @RequestParam(required = false) BigDecimal processScore,
                            @RequestParam(required = false) BigDecimal finalScore,
                            @RequestParam(required = false) String note,
                            RedirectAttributes redirectAttributes) {
        try {
            User currentTeacher = authenticationHelper.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Enrollment enrollment = enrollmentService.findById(enrollmentId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đăng ký"));

             // Verify teacher (Double check security)
            if (enrollment.getClassEntity().getTeacher() == null ||
                !enrollment.getClassEntity().getTeacher().getId().equals(currentTeacher.getId())) {
                throw new RuntimeException("Bạn không có quyền nhập điểm cho lớp này");
            }

            gradeService.saveOrUpdateGrade(enrollment, attendanceScore, processScore, finalScore, currentTeacher, note);

            redirectAttributes.addFlashAttribute("success", "Cập nhật điểm thành công cho " + enrollment.getStudent().getFullName());
            return "redirect:/teacher/grades/class/" + enrollment.getClassEntity().getId();

        } catch (Exception e) {
            log.error("Error saving grade", e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi lưu điểm: " + e.getMessage());
            // Cần redirect về trang form, nhưng không có đủ info ở đây dễ dàng, redirect về list
            // Muốn tốt hơn thì cần pass classId/studentId trong form hidden field
             return "redirect:/teacher/grades"; 
        }
    }
}
