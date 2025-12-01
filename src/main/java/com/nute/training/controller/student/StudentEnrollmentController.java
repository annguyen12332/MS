package com.nute.training.controller.student;

import com.nute.training.entity.ClassEntity;
import com.nute.training.entity.Course;
import com.nute.training.entity.User;
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
        model.addAttribute("courses", courseService.findActiveCourses());

        if (courseId != null) {
            // Tìm lớp theo khóa học (TODO: ClassService cần method findOpenClassesByCourse)
            // Tạm thời lấy tất cả active classes rồi filter ở view hoặc service
             model.addAttribute("classes", classService.findByStatus(ClassEntity.ClassStatus.PENDING));
             model.addAttribute("selectedCourseId", courseId);
        } else {
             // Mặc định hiển thị các lớp đang tuyển sinh (PENDING)
             model.addAttribute("classes", classService.findByStatus(ClassEntity.ClassStatus.PENDING));
        }
        
        return "student/enrollments/browse";
    }

    /**
     * Xử lý đăng ký lớp học
     */
    @PostMapping("/register")
    public String register(@RequestParam Long classId,
                           @RequestParam(required = false) String notes,
                           RedirectAttributes redirectAttributes) {
        try {
            User currentStudent = authenticationHelper.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ClassEntity classEntity = classService.findById(classId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học"));

            enrollmentService.createEnrollment(currentStudent, classEntity, notes);

            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Vui lòng chờ duyệt.");
            return "redirect:/student/dashboard";
        } catch (Exception e) {
            log.error("Error registering class", e);
            redirectAttributes.addFlashAttribute("error", "Lỗi đăng ký: " + e.getMessage());
            return "redirect:/student/enrollments/browse";
        }
    }

    /**
     * Xem lịch sử đăng ký
     */
    @GetMapping("/history")
    public String history(Model model) {
        try {
             User currentStudent = authenticationHelper.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            model.addAttribute("enrollments", enrollmentService.findByStudent(currentStudent));
            return "student/enrollments/history";
        } catch (Exception e) {
             return "redirect:/login";
        }
    }
}
