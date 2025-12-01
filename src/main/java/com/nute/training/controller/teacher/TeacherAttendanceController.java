package com.nute.training.controller.teacher;

import com.nute.training.entity.Attendance;
import com.nute.training.entity.Schedule;
import com.nute.training.entity.User;
import com.nute.training.service.AttendanceService;
import com.nute.training.service.ClassService;
import com.nute.training.service.EnrollmentService;
import com.nute.training.service.ScheduleService;
import com.nute.training.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller: TeacherAttendanceController
 * Giảng viên điểm danh học viên
 */
@Controller
@RequestMapping("/teacher/attendance")
@RequiredArgsConstructor
@Slf4j
public class TeacherAttendanceController {

    private final AttendanceService attendanceService;
    private final ScheduleService scheduleService;
    private final ClassService classService;
    private final EnrollmentService enrollmentService;
    private final AuthenticationHelper authenticationHelper;

    /**
     * Danh sách lớp đang dạy
     */
    @GetMapping
    public String myClasses(Model model) {
        User currentTeacher = authenticationHelper.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not found"));

        var ongoingClasses = classService.findOngoingClassesByTeacher(currentTeacher);
        model.addAttribute("classes", ongoingClasses);
        return "teacher/attendance/classes";
    }

    /**
     * Danh sách lịch học của lớp
     */
    @GetMapping("/class/{classId}/schedules")
    public String classSchedules(@PathVariable Long classId, Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            User currentTeacher = authenticationHelper.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not found"));

            var classEntity = classService.findById(classId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học"));

            // Verify teacher owns this class
            if (classEntity.getTeacher() == null ||
                !classEntity.getTeacher().getId().equals(currentTeacher.getId())) {
                throw new RuntimeException("Bạn không phải giảng viên của lớp này");
            }

            var schedules = scheduleService.findByClass(classEntity);
            model.addAttribute("classEntity", classEntity);
            model.addAttribute("schedules", schedules);
            return "teacher/attendance/schedules";
        } catch (Exception e) {
            log.error("Error loading schedules", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/teacher/attendance";
        }
    }

    /**
     * Form điểm danh cho buổi học
     */
    @GetMapping("/schedule/{scheduleId}")
    public String attendanceForm(@PathVariable Long scheduleId, Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            User currentTeacher = authenticationHelper.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Schedule schedule = scheduleService.findById(scheduleId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch học"));

            // Verify teacher owns this class
            if (schedule.getClassEntity().getTeacher() == null ||
                !schedule.getClassEntity().getTeacher().getId().equals(currentTeacher.getId())) {
                throw new RuntimeException("Bạn không phải giảng viên của lớp này");
            }

            // Get approved enrollments
            var approvedEnrollments = enrollmentService
                    .findApprovedEnrollmentsByClass(schedule.getClassEntity());

            // Get existing attendances
            var existingAttendances = attendanceService.findBySchedule(schedule);

            model.addAttribute("schedule", schedule);
            model.addAttribute("enrollments", approvedEnrollments);
            model.addAttribute("existingAttendances", existingAttendances);
            model.addAttribute("attendanceStatuses", Attendance.AttendanceStatus.values());
            return "teacher/attendance/form";
        } catch (Exception e) {
            log.error("Error loading attendance form", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/teacher/attendance";
        }
    }

    /**
     * Điểm danh một học viên
     */
    @PostMapping("/schedule/{scheduleId}/mark")
    public String markAttendance(@PathVariable Long scheduleId,
                                @RequestParam Long studentId,
                                @RequestParam Attendance.AttendanceStatus status,
                                @RequestParam(required = false) String note,
                                RedirectAttributes redirectAttributes) {
        try {
            User currentTeacher = authenticationHelper.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Schedule schedule = scheduleService.findById(scheduleId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch học"));

            var student = new User();
            student.setId(studentId);

            attendanceService.markAttendance(schedule, student, status, currentTeacher, note);

            redirectAttributes.addFlashAttribute("success", "Điểm danh thành công");
        } catch (Exception e) {
            log.error("Error marking attendance", e);
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi khi điểm danh: " + e.getMessage());
        }
        return "redirect:/teacher/attendance/schedule/" + scheduleId;
    }

    /**
     * Điểm danh hàng loạt
     */
    @PostMapping("/schedule/{scheduleId}/mark-all")
    public String markAllAttendance(@PathVariable Long scheduleId,
                                   @RequestParam Attendance.AttendanceStatus defaultStatus,
                                   RedirectAttributes redirectAttributes) {
        try {
            User currentTeacher = authenticationHelper.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Schedule schedule = scheduleService.findById(scheduleId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch học"));

            attendanceService.markAttendanceForAllStudents(schedule, defaultStatus, currentTeacher);

            redirectAttributes.addFlashAttribute("success",
                    "Điểm danh tất cả học viên thành công");
        } catch (Exception e) {
            log.error("Error marking all attendances", e);
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi khi điểm danh: " + e.getMessage());
        }
        return "redirect:/teacher/attendance/schedule/" + scheduleId;
    }

    /**
     * Cập nhật điểm danh
     */
    @PostMapping("/attendance/{id}/update")
    public String updateAttendance(@PathVariable Long id,
                                  @RequestParam Attendance.AttendanceStatus status,
                                  @RequestParam(required = false) String note,
                                  @RequestParam Long scheduleId,
                                  RedirectAttributes redirectAttributes) {
        try {
            attendanceService.updateAttendance(id, status, note);
            redirectAttributes.addFlashAttribute("success",
                    "Cập nhật điểm danh thành công");
        } catch (Exception e) {
            log.error("Error updating attendance", e);
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi khi cập nhật điểm danh: " + e.getMessage());
        }
        return "redirect:/teacher/attendance/schedule/" + scheduleId;
    }

    /**
     * Xem thống kê điểm danh của lớp
     */
    @GetMapping("/class/{classId}/statistics")
    public String attendanceStatistics(@PathVariable Long classId, Model model,
                                       RedirectAttributes redirectAttributes) {
        try {
            User currentTeacher = authenticationHelper.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not found"));

            var classEntity = classService.findById(classId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học"));

            // Verify teacher owns this class
            if (classEntity.getTeacher() == null ||
                !classEntity.getTeacher().getId().equals(currentTeacher.getId())) {
                throw new RuntimeException("Bạn không phải giảng viên của lớp này");
            }

            var approvedEnrollments = enrollmentService
                    .findApprovedEnrollmentsByClass(classEntity);

            model.addAttribute("classEntity", classEntity);
            model.addAttribute("enrollments", approvedEnrollments);
            return "teacher/attendance/statistics";
        } catch (Exception e) {
            log.error("Error loading attendance statistics", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/teacher/attendance";
        }
    }
}
