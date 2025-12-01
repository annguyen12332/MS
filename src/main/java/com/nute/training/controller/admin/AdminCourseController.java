package com.nute.training.controller.admin;

import com.nute.training.entity.Course;
import com.nute.training.entity.User;
import com.nute.training.service.CourseService;
import com.nute.training.service.CourseTypeService;
import com.nute.training.util.AuthenticationHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller: AdminCourseController
 * Quản lý khóa học (CRUD)
 */
@Controller
@RequestMapping("/admin/courses")
@RequiredArgsConstructor
@Slf4j
public class AdminCourseController {

    private final CourseService courseService;
    private final CourseTypeService courseTypeService;
    private final AuthenticationHelper authenticationHelper;

    /**
     * Danh sách khóa học
     */
    @GetMapping
    public String list(@RequestParam(required = false) String keyword, Model model) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            model.addAttribute("courses", courseService.searchCourses(keyword));
            model.addAttribute("keyword", keyword);
        } else {
            model.addAttribute("courses", courseService.findAll());
        }
        return "admin/courses/list";
    }

    /**
     * Form tạo khóa học mới
     */
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("course", new Course());
        model.addAttribute("courseTypes", courseTypeService.findAll());
        model.addAttribute("statuses", Course.CourseStatus.values());
        return "admin/courses/form";
    }

    /**
     * Xử lý tạo khóa học
     */
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute Course course,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("courseTypes", courseTypeService.findAll());
            model.addAttribute("statuses", Course.CourseStatus.values());
            return "admin/courses/form";
        }

        try {
            // Set created by
            User currentUser = authenticationHelper.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not found"));
            course.setCreatedBy(currentUser);

            courseService.createCourse(course);
            redirectAttributes.addFlashAttribute("success",
                    "Tạo khóa học thành công: " + course.getName());
            return "redirect:/admin/courses";
        } catch (Exception e) {
            log.error("Error creating course", e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("courseTypes", courseTypeService.findAll());
            model.addAttribute("statuses", Course.CourseStatus.values());
            return "admin/courses/form";
        }
    }

    /**
     * Form chỉnh sửa khóa học
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Course course = courseService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học"));

            model.addAttribute("course", course);
            model.addAttribute("courseTypes", courseTypeService.findAll());
            model.addAttribute("statuses", Course.CourseStatus.values());
            return "admin/courses/form";
        } catch (Exception e) {
            log.error("Error loading course for edit", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/courses";
        }
    }

    /**
     * Xử lý cập nhật khóa học
     */
    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute Course course,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("courseTypes", courseTypeService.findAll());
            model.addAttribute("statuses", Course.CourseStatus.values());
            return "admin/courses/form";
        }

        try {
            courseService.updateCourse(id, course);
            redirectAttributes.addFlashAttribute("success",
                    "Cập nhật khóa học thành công: " + course.getName());
            return "redirect:/admin/courses";
        } catch (Exception e) {
            log.error("Error updating course", e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("courseTypes", courseTypeService.findAll());
            model.addAttribute("statuses", Course.CourseStatus.values());
            return "admin/courses/form";
        }
    }

    /**
     * Xem chi tiết khóa học
     */
    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Course course = courseService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học"));

            model.addAttribute("course", course);
            return "admin/courses/view";
        } catch (Exception e) {
            log.error("Error loading course", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/courses";
        }
    }

    /**
     * Xóa khóa học
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Course course = courseService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học"));

            String courseName = course.getName();
            courseService.deleteCourse(id);

            redirectAttributes.addFlashAttribute("success",
                    "Xóa khóa học thành công: " + courseName);
        } catch (Exception e) {
            log.error("Error deleting course", e);
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi khi xóa khóa học: " + e.getMessage());
        }
        return "redirect:/admin/courses";
    }

    /**
     * Thay đổi trạng thái khóa học
     */
    @PostMapping("/{id}/change-status")
    public String changeStatus(@PathVariable Long id,
                              @RequestParam Course.CourseStatus status,
                              RedirectAttributes redirectAttributes) {
        try {
            courseService.changeStatus(id, status);
            redirectAttributes.addFlashAttribute("success",
                    "Thay đổi trạng thái khóa học thành công");
        } catch (Exception e) {
            log.error("Error changing course status", e);
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi khi thay đổi trạng thái: " + e.getMessage());
        }
        return "redirect:/admin/courses";
    }
}
