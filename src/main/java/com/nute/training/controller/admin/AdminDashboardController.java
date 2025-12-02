package com.nute.training.controller.admin;

import com.nute.training.entity.ClassEntity;
import com.nute.training.entity.Course;
import com.nute.training.entity.Enrollment;
import com.nute.training.entity.User;
import com.nute.training.service.*;
import com.nute.training.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Controller: AdminDashboardController
 * Dashboard cho Admin
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final UserService userService;
    private final CourseService courseService;
    private final ClassService classService;
    private final EnrollmentService enrollmentService;
    private final CertificateService certificateService;
    private final AuthenticationHelper authenticationHelper;

    /**
     * Trang dashboard admin
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Get current admin
        User currentAdmin = authenticationHelper.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Statistics
        long totalUsers = userService.findAll().size();
        long totalTeachers = userService.findActiveTeachers().size();
        long totalStudents = userService.findActiveStudents().size();
        long totalCourses = courseService.findAll().size();
        long activeCourses = courseService.findActiveCourses().size();
        long totalClasses = classService.findAll().size();
        long ongoingClasses = classService.findByStatus(ClassEntity.ClassStatus.ONGOING).size();
        long pendingEnrollments = enrollmentService.findPendingEnrollments().size();

        // Recent data
        List<Course> recentCourses = courseService.findActiveCourses().stream().limit(5).toList();
        List<ClassEntity> recentClasses = classService.findByStatus(ClassEntity.ClassStatus.ONGOING).stream().limit(5).toList();
        List<Enrollment> recentEnrollments = enrollmentService.findPendingEnrollments().stream().limit(10).toList();

        // Add to model
        model.addAttribute("currentUser", currentAdmin);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalTeachers", totalTeachers);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("activeCourses", activeCourses);
        model.addAttribute("totalClasses", totalClasses);
        model.addAttribute("ongoingClasses", ongoingClasses);
        model.addAttribute("pendingEnrollments", pendingEnrollments);
        model.addAttribute("recentCourses", recentCourses);
        model.addAttribute("recentClasses", recentClasses);
        model.addAttribute("recentEnrollments", recentEnrollments);
        model.addAttribute("pageTitle", "Dashboard");

        return "admin/dashboard";
    }
}
