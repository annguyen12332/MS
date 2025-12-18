package com.nute.training.controller.student;

import com.nute.training.entity.User;
import com.nute.training.exception.UnauthorizedException;
import com.nute.training.service.AttendanceService;
import com.nute.training.service.EnrollmentService;
import com.nute.training.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller: StudentAttendanceController
 * Học viên xem lịch sử điểm danh
 */
@Controller
@RequestMapping("/student/attendance")
@RequiredArgsConstructor
@Slf4j
public class StudentAttendanceController {

    private final AttendanceService attendanceService;
    private final EnrollmentService enrollmentService;
    private final AuthenticationHelper authenticationHelper;

    /**
     * Xem lịch sử điểm danh của tất cả các lớp
     */
    @GetMapping
    public String myAttendance(Model model) {
        User currentStudent = authenticationHelper.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException());

        // Get all approved enrollments
        var enrollments = enrollmentService.findByStudent(currentStudent).stream()
                .filter(e -> e.getStatus() == com.nute.training.entity.Enrollment.EnrollmentStatus.APPROVED)
                .toList();

        model.addAttribute("enrollments", enrollments);
        model.addAttribute("pageTitle", "Lịch sử điểm danh");
        return "student/attendance/history";
    }

    /**
     * Xem chi tiết điểm danh của một lớp
     */
    @GetMapping("/class/{classId}")
    public String classAttendance(@PathVariable Long classId, Model model) {
        User currentStudent = authenticationHelper.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException());

        // Get attendance records for this class
        var attendances = attendanceService.findStudentAttendanceByClass(currentStudent, classId);

        // Calculate statistics
        Long presentCount = attendanceService.countPresentAttendances(currentStudent.getId(), classId);
        Long absentCount = attendanceService.countAbsentAttendances(currentStudent.getId(), classId);
        Double attendanceRate = attendanceService.calculateAttendanceRate(currentStudent.getId(), classId);

        model.addAttribute("attendances", attendances);
        model.addAttribute("presentCount", presentCount);
        model.addAttribute("absentCount", absentCount);
        model.addAttribute("attendanceRate", attendanceRate);
        model.addAttribute("pageTitle", "Chi tiết điểm danh");

        if (!attendances.isEmpty()) {
            model.addAttribute("classEntity", attendances.get(0).getSchedule().getClassEntity());
        }

        return "student/attendance/detail";
    }
}
