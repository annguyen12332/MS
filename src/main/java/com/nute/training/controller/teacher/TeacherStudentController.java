package com.nute.training.controller.teacher;

import com.nute.training.entity.ClassEntity;
import com.nute.training.entity.Enrollment;
import com.nute.training.entity.Grade;
import com.nute.training.entity.User;
import com.nute.training.service.*;
import com.nute.training.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller: TeacherStudentController
 * Xem thông tin chi tiết học viên
 */
@Controller
@RequestMapping("/teacher/students")
@RequiredArgsConstructor
@Slf4j
public class TeacherStudentController {

    private final UserService userService;
    private final ClassService classService;
    private final EnrollmentService enrollmentService;
    private final GradeService gradeService;
    private final AttendanceService attendanceService;
    private final AuthenticationHelper authenticationHelper;

    /**
     * Chi tiết học viên trong một lớp học
     */
    @GetMapping("/{studentId}")
    public String studentDetail(
            @PathVariable Long studentId,
            @RequestParam Long classId,
            Model model) {
        try {
            User currentTeacher = authenticationHelper.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Lấy thông tin lớp học
            ClassEntity classEntity = classService.findById(classId)
                    .orElseThrow(() -> new RuntimeException("Class not found"));

            // Kiểm tra quyền truy cập
            if (!classEntity.getTeacher().getId().equals(currentTeacher.getId())) {
                log.warn("Teacher {} attempted to access student in class {} belonging to another teacher",
                        currentTeacher.getId(), classId);
                model.addAttribute("error", "Bạn không có quyền truy cập lớp học này");
                return "redirect:/teacher/classes";
            }

            // Lấy thông tin học viên
            User student = userService.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            // Lấy thông tin enrollment
            Enrollment enrollment = enrollmentService.findByStudentAndClass(student, classEntity)
                    .orElseThrow(() -> new RuntimeException("Enrollment not found"));

            // Lấy điểm
            Grade grade = gradeService.findByEnrollment(enrollment).orElse(null);

            // Tính tỷ lệ điểm danh
            double attendanceRate = attendanceService.calculateAttendanceRate(studentId, classId);

            // Lấy lịch sử điểm danh
            var attendanceRecords = attendanceService.findStudentAttendanceByClass(student, classId);

            model.addAttribute("student", student);
            model.addAttribute("classEntity", classEntity);
            model.addAttribute("enrollment", enrollment);
            model.addAttribute("grade", grade);
            model.addAttribute("attendanceRate", attendanceRate);
            model.addAttribute("attendanceRecords", attendanceRecords);
            model.addAttribute("pageTitle", "Chi tiết học viên: " + student.getFullName());

            return "teacher/students/detail";
        } catch (Exception e) {
            log.error("Error loading student detail", e);
            model.addAttribute("error", "Không thể tải thông tin học viên");
            return "redirect:/teacher/classes";
        }
    }
}
