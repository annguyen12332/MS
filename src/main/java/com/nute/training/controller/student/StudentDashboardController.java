package com.nute.training.controller.student;

import com.nute.training.entity.Enrollment;
import com.nute.training.entity.User;
import com.nute.training.service.EnrollmentService;
import com.nute.training.service.ScheduleService;
import com.nute.training.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Controller: StudentDashboardController
 * Trang chủ dành cho học viên
 */
@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
@Slf4j
public class StudentDashboardController {

    private final EnrollmentService enrollmentService;
    private final ScheduleService scheduleService;
    private final AuthenticationHelper authenticationHelper;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            User currentStudent = authenticationHelper.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Các khóa học đang tham gia (APPROVED)
            List<Enrollment> myEnrollments = enrollmentService.findByStudent(currentStudent);
            long activeCourses = myEnrollments.stream()
                    .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.APPROVED)
                    .count();

            // Lịch học sắp tới
            var upcomingSchedules = scheduleService.findUpcomingSchedulesForStudent(currentStudent);
            
            model.addAttribute("enrollments", myEnrollments);
            model.addAttribute("activeCoursesCount", activeCourses);
            model.addAttribute("studentName", currentStudent.getFullName());
            model.addAttribute("upcomingSchedules", upcomingSchedules);

            return "student/dashboard";
        } catch (Exception e) {
            log.error("Error loading student dashboard", e);
            return "redirect:/login";
        }
    }
}
