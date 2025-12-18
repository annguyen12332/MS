package com.nute.training.controller.teacher;

import com.nute.training.entity.ClassEntity;
import com.nute.training.entity.ClassEntity.ClassStatus;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller: TeacherClassController
 * Quản lý lớp học của giảng viên
 */
@Controller
@RequestMapping("/teacher/classes")
@RequiredArgsConstructor
@Slf4j
public class TeacherClassController {

    private final ClassService classService;
    private final EnrollmentService enrollmentService;
    private final GradeService gradeService;
    private final AttendanceService attendanceService;
    private final AuthenticationHelper authenticationHelper;

    /**
     * Danh sách lớp học của giảng viên
     */
    @GetMapping
    public String listClasses(@RequestParam(required = false) ClassStatus status, Model model) {
        try {
            User currentTeacher = authenticationHelper.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Lấy tất cả lớp của giảng viên
            List<ClassEntity> allClasses = classService.findByTeacher(currentTeacher);

            // Lọc theo status nếu có
            List<ClassEntity> filteredClasses = status != null
                    ? allClasses.stream().filter(c -> c.getStatus() == status).collect(Collectors.toList())
                    : allClasses;

            // Đếm số lượng theo status
            long totalCount = allClasses.size();
            long ongoingCount = allClasses.stream().filter(c -> c.getStatus() == ClassStatus.ONGOING).count();
            long pendingCount = allClasses.stream().filter(c -> c.getStatus() == ClassStatus.PENDING).count();
            long completedCount = allClasses.stream().filter(c -> c.getStatus() == ClassStatus.COMPLETED).count();

            model.addAttribute("classes", filteredClasses);
            model.addAttribute("status", status);
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("ongoingCount", ongoingCount);
            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("completedCount", completedCount);
            model.addAttribute("pageTitle", "Lớp học của tôi");

            return "teacher/classes/list";
        } catch (Exception e) {
            log.error("Error loading teacher classes", e);
            model.addAttribute("error", "Không thể tải danh sách lớp học");
            return "redirect:/teacher/dashboard";
        }
    }

    /**
     * Chi tiết lớp học
     */
    @GetMapping("/{id}")
    public String classDetail(@PathVariable Long id, Model model) {
        try {
            User currentTeacher = authenticationHelper.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Lấy thông tin lớp học
            ClassEntity classEntity = classService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Class not found"));

            // Kiểm tra quyền truy cập
            if (!classEntity.getTeacher().getId().equals(currentTeacher.getId())) {
                log.warn("Teacher {} attempted to access class {} belonging to another teacher",
                        currentTeacher.getId(), id);
                model.addAttribute("error", "Bạn không có quyền truy cập lớp học này");
                return "redirect:/teacher/classes";
            }

            // Lấy danh sách học viên đã được approved
            List<User> students = enrollmentService.findApprovedEnrollmentsByClass(classEntity)
                    .stream()
                    .map(e -> e.getStudent())
                    .collect(Collectors.toList());

            // Lấy danh sách điểm
            List<Grade> grades = gradeService.findGradesByClass(classEntity.getId());

            // Tính toán thống kê
            double avgGrade = grades.isEmpty() ? 0.0 :
                    grades.stream().mapToDouble(g -> g.getTotalScore().doubleValue()).average().orElse(0.0);

            long passedCount = grades.stream().filter(g -> g.getPass()).count();
            double passRate = grades.isEmpty() ? 0.0 : (passedCount * 100.0 / grades.size());

            // Tính toán điểm danh trung bình cho từng học viên
            Map<Long, Double> studentAttendance = new HashMap<>();
            for (User student : students) {
                double attendanceRate = attendanceService.calculateAttendanceRate(student.getId(), classEntity.getId());
                studentAttendance.put(student.getId(), attendanceRate);
            }

            // Tính điểm danh trung bình của lớp
            double avgAttendance = studentAttendance.isEmpty() ? 0.0 :
                    studentAttendance.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

            model.addAttribute("classEntity", classEntity);
            model.addAttribute("students", students);
            model.addAttribute("grades", grades);
            model.addAttribute("avgGrade", avgGrade);
            model.addAttribute("passRate", passRate);
            model.addAttribute("avgAttendance", avgAttendance);
            model.addAttribute("studentAttendance", studentAttendance);
            model.addAttribute("pageTitle", classEntity.getClassName());

            return "teacher/classes/detail";
        } catch (Exception e) {
            log.error("Error loading class detail", e);
            model.addAttribute("error", "Không thể tải chi tiết lớp học");
            return "redirect:/teacher/classes";
        }
    }
}
