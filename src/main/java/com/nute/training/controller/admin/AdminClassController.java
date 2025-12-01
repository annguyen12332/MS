package com.nute.training.controller.admin;

import com.nute.training.entity.ClassEntity;
import com.nute.training.service.ClassService;
import com.nute.training.service.CourseService;
import com.nute.training.service.EnrollmentService;
import com.nute.training.service.ScheduleService;
import com.nute.training.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller: AdminClassController
 * Quản lý lớp học + Lập thời khóa biểu
 */
@Controller
@RequestMapping("/admin/classes")
@RequiredArgsConstructor
@Slf4j
public class AdminClassController {

    private final ClassService classService;
    private final CourseService courseService;
    private final UserService userService;
    private final ScheduleService scheduleService;
    private final EnrollmentService enrollmentService;

    /**
     * Danh sách lớp học
     */
    @GetMapping
    public String list(@RequestParam(required = false) String status,
                      @RequestParam(required = false) String keyword,
                      Model model) {

        if (keyword != null && !keyword.trim().isEmpty()) {
            model.addAttribute("classes", classService.searchClasses(keyword));
            model.addAttribute("keyword", keyword);
        } else if (status != null && !status.isEmpty()) {
            ClassEntity.ClassStatus classStatus = ClassEntity.ClassStatus.valueOf(status);
            model.addAttribute("classes", classService.findByStatus(classStatus));
            model.addAttribute("selectedStatus", status);
        } else {
            model.addAttribute("classes", classService.findAll());
        }

        model.addAttribute("statuses", ClassEntity.ClassStatus.values());
        return "admin/classes/list";
    }

    /**
     * Form tạo lớp học mới
     */
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("classEntity", new ClassEntity());
        model.addAttribute("courses", courseService.findActiveCourses());
        model.addAttribute("teachers", userService.findActiveTeachers());
        model.addAttribute("statuses", ClassEntity.ClassStatus.values());
        return "admin/classes/form";
    }

    /**
     * Xử lý tạo/cập nhật lớp học (Unified save endpoint)
     */
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("classEntity") ClassEntity classEntity,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("courses", courseService.findActiveCourses());
            model.addAttribute("teachers", userService.findActiveTeachers());
            model.addAttribute("statuses", ClassEntity.ClassStatus.values());
            return "admin/classes/form";
        }

        try {
            if (classEntity.getId() == null) {
                classService.createClass(classEntity);
                redirectAttributes.addFlashAttribute("success",
                        "Tạo lớp học thành công: " + classEntity.getClassName());
            } else {
                classService.updateClass(classEntity.getId(), classEntity);
                redirectAttributes.addFlashAttribute("success",
                        "Cập nhật lớp học thành công: " + classEntity.getClassName());
            }
            return "redirect:/admin/classes";
        } catch (Exception e) {
            log.error("Error saving class", e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("courses", courseService.findActiveCourses());
            model.addAttribute("teachers", userService.findActiveTeachers());
            model.addAttribute("statuses", ClassEntity.ClassStatus.values());
            return "admin/classes/form";
        }
    }

    /**
     * Form chỉnh sửa lớp học
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model,
                          RedirectAttributes redirectAttributes) {
        try {
            ClassEntity classEntity = classService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học"));

            model.addAttribute("classEntity", classEntity);
            model.addAttribute("courses", courseService.findActiveCourses());
            model.addAttribute("teachers", userService.findActiveTeachers());
            model.addAttribute("statuses", ClassEntity.ClassStatus.values());
            return "admin/classes/form";
        } catch (Exception e) {
            log.error("Error loading class for edit", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/classes";
        }
    }

    /**
     * Xem chi tiết lớp học (bao gồm thời khóa biểu và danh sách học viên)
     */
    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model,
                      RedirectAttributes redirectAttributes) {
        try {
            ClassEntity classEntity = classService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học"));

            model.addAttribute("classEntity", classEntity);
            model.addAttribute("schedules", scheduleService.findByClass(classEntity));
            model.addAttribute("enrollments", enrollmentService.findApprovedEnrollmentsByClass(classEntity));
            return "admin/classes/view";
        } catch (Exception e) {
            log.error("Error loading class", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/classes";
        }
    }

    /**
     * Xóa lớp học
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ClassEntity classEntity = classService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học"));

            String className = classEntity.getClassName();
            classService.deleteClass(id);

            redirectAttributes.addFlashAttribute("success",
                    "Xóa lớp học thành công: " + className);
        } catch (Exception e) {
            log.error("Error deleting class", e);
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi khi xóa lớp học: " + e.getMessage());
        }
        return "redirect:/admin/classes";
    }

    /**
     * Phân công giảng viên
     */
    @PostMapping("/{id}/assign-teacher")
    public String assignTeacher(@PathVariable Long id,
                               @RequestParam Long teacherId,
                               RedirectAttributes redirectAttributes) {
        try {
            var teacher = userService.findById(teacherId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy giảng viên"));

            classService.assignTeacher(id, teacher);

            redirectAttributes.addFlashAttribute("success",
                    "Phân công giảng viên thành công: " + teacher.getFullName());
        } catch (Exception e) {
            log.error("Error assigning teacher", e);
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi khi phân công giảng viên: " + e.getMessage());
        }
        return "redirect:/admin/classes/" + id;
    }

    /**
     * Thay đổi trạng thái lớp
     */
    @PostMapping("/{id}/change-status")
    public String changeStatus(@PathVariable Long id,
                              @RequestParam ClassEntity.ClassStatus status,
                              RedirectAttributes redirectAttributes) {
        try {
            classService.changeStatus(id, status);
            redirectAttributes.addFlashAttribute("success",
                    "Thay đổi trạng thái lớp học thành công");
        } catch (Exception e) {
            log.error("Error changing class status", e);
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi khi thay đổi trạng thái: " + e.getMessage());
        }
        return "redirect:/admin/classes/" + id;
    }
}
