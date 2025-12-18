package com.nute.training.controller.student;

import com.nute.training.entity.ClassEntity;
import com.nute.training.entity.Course;
import com.nute.training.entity.User;
import com.nute.training.exception.BusinessException;
import com.nute.training.exception.ResourceNotFoundException;
import com.nute.training.exception.UnauthorizedException;
import com.nute.training.service.ClassService;
import com.nute.training.service.CourseService;
import com.nute.training.service.EnrollmentService;
import com.nute.training.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller: StudentEnrollmentController
 * Học viên đăng ký khóa học
 */
@Controller
@RequestMapping("/student/enrollments")
@RequiredArgsConstructor
@Slf4j
public class StudentEnrollmentController {

    private final ClassService classService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final AuthenticationHelper authenticationHelper;

    /**
     * Danh sách các khóa học/lớp học đang mở đăng ký
     */
    @GetMapping("/browse")
    public String browseClasses(@RequestParam(required = false) Long courseId, Model model) {
        // Lấy danh sách khóa học để filter
        var courses = courseService.findActiveCourses();
        log.info("Found {} active courses", courses.size());
        model.addAttribute("courses", courses);
        model.addAttribute("pageTitle", "Tìm lớp học");

        List<ClassEntity> classes;
        if (courseId != null) {
            // Tìm lớp theo khóa học
            Course course = courseService.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Khóa học", courseId));
            classes = classService.findOpenClassesByCourse(course);
            log.info("Found {} classes for course ID: {}", classes.size(), courseId);
            model.addAttribute("selectedCourseId", courseId);
        } else {
            // Mặc định hiển thị các lớp đang tuyển sinh (PENDING và ONGOING)
            classes = classService.findAvailableClasses();
            log.info("Found {} available classes (PENDING or ONGOING with seats)", classes.size());
        }

        model.addAttribute("classes", classes);

        // Debug log chi tiết
        classes.forEach(c -> log.debug("Class: {} - Status: {} - Students: {}/{}",
            c.getClassName(), c.getStatus(), c.getCurrentStudents(), c.getMaxStudents()));

        return "student/enrollments/browse";
    }

    /**
     * Xử lý đăng ký lớp học
     */
    @PostMapping("/register")
    public String register(@RequestParam Long classId,
                           @RequestParam(required = false) String notes,
                           RedirectAttributes redirectAttributes) {
        User currentStudent = authenticationHelper.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException());

        ClassEntity classEntity = classService.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Lớp học", classId));

        try {
            enrollmentService.createEnrollment(currentStudent, classEntity, notes);
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Vui lòng chờ duyệt.");
            return "redirect:/student/dashboard";
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Business logic errors (lớp đầy, đã đăng ký, v.v.)
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Xem lịch sử đăng ký (Optimized with DTO projection)
     */
    @GetMapping("/history")
    public String history(Model model) {
        User currentStudent = authenticationHelper.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException());

        model.addAttribute("enrollments", enrollmentService.findEnrollmentHistoryByStudent(currentStudent));
        model.addAttribute("pageTitle", "Lớp học của tôi");
        return "student/enrollments/history";
    }
}
