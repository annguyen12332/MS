package com.nute.training.controller.teacher;

import com.nute.training.entity.User;
import com.nute.training.service.ClassService;
import com.nute.training.service.ScheduleService;
import com.nute.training.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    private final AuthenticationHelper authenticationHelper;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            User currentTeacher = authenticationHelper.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Thống kê cơ bản
            var myClasses = classService.findOngoingClassesByTeacher(currentTeacher);
            var upcomingSchedules = scheduleService.findUpcomingSchedulesForTeacher(currentTeacher);

            model.addAttribute("activeClassesCount", myClasses.size());
            model.addAttribute("upcomingSchedules", upcomingSchedules);
            model.addAttribute("teacherName", currentTeacher.getFullName());

            return "teacher/dashboard";
        } catch (Exception e) {
            log.error("Error loading teacher dashboard", e);
            return "redirect:/login";
        }
    }
}
