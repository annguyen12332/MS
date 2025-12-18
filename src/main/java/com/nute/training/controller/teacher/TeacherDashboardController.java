package com.nute.training.controller.teacher;

import com.nute.training.entity.ClassEntity;
import com.nute.training.entity.User;
import com.nute.training.service.*;
import com.nute.training.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller: TeacherDashboardController
 * Trang chủ dành cho giảng viên
 */
@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
@Slf4j
public class TeacherDashboardController {

    private final ClassService classService;
    private final ScheduleService scheduleService;
    private final EnrollmentService enrollmentService;
    private final GradeService gradeService;
    private final AuthenticationHelper authenticationHelper;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            User currentTeacher = authenticationHelper.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Lấy danh sách lớp đang dạy
            var myClasses = classService.findOngoingClassesByTeacher(currentTeacher);
            var allClasses = classService.findByTeacher(currentTeacher);

            // Lấy lịch giảng dạy sắp tới (7 ngày)
            var upcomingSchedules = scheduleService.findUpcomingSchedulesForTeacher(currentTeacher);

            // Tính tổng số học viên
            long totalStudentsCount = myClasses.stream()
                    .mapToLong(ClassEntity::getCurrentStudents)
                    .sum();

            // Tính số buổi học trong tuần
            LocalDate startOfWeek = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
            LocalDate endOfWeek = startOfWeek.plusDays(6);
            long weeklySessionsCount = upcomingSchedules.stream()
                    .filter(s -> !s.getSessionDate().isBefore(startOfWeek) &&
                                !s.getSessionDate().isAfter(endOfWeek))
                    .count();

            // Đếm số điểm chưa nhập (enrollment đã approved nhưng chưa có grade)
            long pendingGradesCount = myClasses.stream()
                    .mapToLong(classEntity -> {
                        var approvedEnrollments = enrollmentService.findApprovedEnrollmentsByClass(classEntity);
                        var gradesCount = gradeService.findGradesByClass(classEntity.getId()).size();
                        return Math.max(0, approvedEnrollments.size() - gradesCount);
                    })
                    .sum();

            // Thêm attributes
            model.addAttribute("activeClassesCount", myClasses.size());
            model.addAttribute("myClasses", myClasses.stream().limit(5).toList());
            model.addAttribute("upcomingSchedules", upcomingSchedules.stream().limit(5).toList());
            model.addAttribute("totalStudentsCount", totalStudentsCount);
            model.addAttribute("weeklySessionsCount", weeklySessionsCount);
            model.addAttribute("pendingGradesCount", pendingGradesCount);
            model.addAttribute("teacherName", currentTeacher.getFullName());
            model.addAttribute("pageTitle", "Dashboard");

            return "teacher/dashboard";
        } catch (Exception e) {
            log.error("Error loading teacher dashboard", e);
            return "redirect:/login";
        }
    }
}
