package com.nute.training.controller.student;

import com.nute.training.dto.StudentGradeDto;
import com.nute.training.entity.Enrollment;
import com.nute.training.entity.Grade;
import com.nute.training.entity.User;
import com.nute.training.service.EnrollmentService;
import com.nute.training.service.GradeService;
import com.nute.training.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

/**
 * Controller: StudentGradeController
 * Học viên xem điểm
 */
@Controller
@RequestMapping("/student/grades")
@RequiredArgsConstructor
@Slf4j
public class StudentGradeController {

    private final GradeService gradeService;
    private final EnrollmentService enrollmentService;
    private final AuthenticationHelper authenticationHelper;

    @GetMapping
    public String myGrades(Model model) {
        try {
            User currentStudent = authenticationHelper.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Lấy tất cả enrollment đã được duyệt (APPROVED, COMPLETED)
            List<Enrollment> enrollments = enrollmentService.findApprovedEnrollmentsByStudent(currentStudent.getId());
            log.info("Found {} approved enrollments for student {}", enrollments.size(), currentStudent.getUsername());

            // Tạo danh sách StudentGradeDto kết hợp enrollment + grade
            List<StudentGradeDto> gradeList = enrollments.stream()
                    .map(enrollment -> {
                        // Tìm grade cho enrollment này (có thể null nếu chưa chấm điểm)
                        Optional<Grade> grade = gradeService.findByEnrollment(enrollment);
                        return new StudentGradeDto(enrollment, grade.orElse(null));
                    })
                    .toList();

            log.info("Created {} StudentGradeDto records for student {}", gradeList.size(), currentStudent.getUsername());

            // Debug: log thông tin các lớp
            for (int i = 0; i < gradeList.size(); i++) {
                StudentGradeDto dto = gradeList.get(i);
                log.info("[{}] Enrollment ID: {}, Class: {}, Course: {}, Has Grade: {}, Status: {}",
                        i + 1,
                        dto.getEnrollment().getId(),
                        dto.getClassName(),
                        dto.getCourseName(),
                        dto.hasGrade(),
                        dto.getEnrollmentStatus());
                if (dto.hasGrade()) {
                    log.info("    Grade ID: {}, Total Score: {}, Pass: {}",
                            dto.getGrade().getId(),
                            dto.getTotalScore(),
                            dto.isPass());
                }
            }

            log.info("Total gradeList size being sent to template: {}", gradeList.size());

            model.addAttribute("gradeList", gradeList);
            model.addAttribute("pageTitle", "Điểm số");

            return "student/grades/list";
        } catch (Exception e) {
            log.error("Error loading student grades", e);
            return "redirect:/student/dashboard";
        }
    }
}
