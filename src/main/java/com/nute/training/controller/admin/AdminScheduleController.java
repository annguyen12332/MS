package com.nute.training.controller.admin;

import com.nute.training.entity.Schedule;
import com.nute.training.service.ClassService;
import com.nute.training.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller: AdminScheduleController
 * Lập thời khóa biểu cho lớp học
 */
@Controller
@RequestMapping("/admin/schedules")
@RequiredArgsConstructor
@Slf4j
public class AdminScheduleController {

    private final ScheduleService scheduleService;
    private final ClassService classService;

    /**
     * Redirect to classes page if accessing root path
     */
    @GetMapping("")
    public String index() {
        return "redirect:/admin/classes";
    }

    /**
     * Xem thời khóa biểu của lớp
     */
    @GetMapping("/class/{classId}")
    public String viewSchedules(@PathVariable Long classId, Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            var classEntity = classService.findById(classId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học"));

            var schedules = scheduleService.findByClass(classEntity);

            model.addAttribute("classEntity", classEntity);
            model.addAttribute("schedules", schedules);
            return "admin/schedules/list";
        } catch (Exception e) {
            log.error("Error loading schedules", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/classes";
        }
    }

    /**
     * Form tạo lịch học mới
     */
    @GetMapping("/class/{classId}/create")
    public String createForm(@PathVariable Long classId, Model model,
                            RedirectAttributes redirectAttributes) {
        try {
            var classEntity = classService.findById(classId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học"));

            Schedule schedule = new Schedule();
            schedule.setClassEntity(classEntity);

            model.addAttribute("schedule", schedule);
            model.addAttribute("classEntity", classEntity);
            model.addAttribute("statuses", Schedule.ScheduleStatus.values());
            return "admin/schedules/form";
        } catch (Exception e) {
            log.error("Error loading create form", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/classes";
        }
    }

    /**
     * Xử lý tạo lịch học
     */
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute Schedule schedule,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            if (schedule.getClassEntity() != null && schedule.getClassEntity().getId() != null) {
                classService.findById(schedule.getClassEntity().getId())
                        .ifPresent(c -> model.addAttribute("classEntity", c));
            }
            model.addAttribute("statuses", Schedule.ScheduleStatus.values());
            return "admin/schedules/form";
        }

        try {
            scheduleService.createSchedule(schedule);
            redirectAttributes.addFlashAttribute("success",
                    "Tạo lịch học thành công - Buổi " + schedule.getSessionNumber());
            return "redirect:/admin/schedules/class/" + schedule.getClassEntity().getId();
        } catch (Exception e) {
            log.error("Error creating schedule", e);
            if (schedule.getClassEntity() != null && schedule.getClassEntity().getId() != null) {
                classService.findById(schedule.getClassEntity().getId())
                        .ifPresent(c -> model.addAttribute("classEntity", c));
            }
            model.addAttribute("error", e.getMessage());
            model.addAttribute("statuses", Schedule.ScheduleStatus.values());
            return "admin/schedules/form";
        }
    }

    /**
     * Form tạo lịch học hàng loạt
     */
    @GetMapping("/class/{classId}/bulk-create")
    public String bulkCreateForm(@PathVariable Long classId, Model model,
                            RedirectAttributes redirectAttributes) {
        try {
            var classEntity = classService.findById(classId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học"));

            com.nute.training.dto.BulkScheduleCreateDto dto = new com.nute.training.dto.BulkScheduleCreateDto();
            dto.setClassId(classId);
            // Default room
            dto.setRoom(classEntity.getRoom());

            model.addAttribute("dto", dto);
            model.addAttribute("classEntity", classEntity);
            return "admin/schedules/bulk-form";
        } catch (Exception e) {
            log.error("Error loading bulk create form", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/classes";
        }
    }

    /**
     * Xử lý tạo lịch học hàng loạt
     */
    @PostMapping("/bulk-create")
    public String bulkCreate(@Valid @ModelAttribute("dto") com.nute.training.dto.BulkScheduleCreateDto dto,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            if (dto.getClassId() != null) {
                classService.findById(dto.getClassId())
                        .ifPresent(c -> model.addAttribute("classEntity", c));
            }
            return "admin/schedules/bulk-form";
        }

        try {
            var schedules = scheduleService.generateBatchSchedules(dto);
            redirectAttributes.addFlashAttribute("success",
                    "Đã tạo thành công " + schedules.size() + " buổi học");
            return "redirect:/admin/schedules/class/" + dto.getClassId();
        } catch (Exception e) {
            log.error("Error bulk creating schedules", e);
            if (dto.getClassId() != null) {
                classService.findById(dto.getClassId())
                        .ifPresent(c -> model.addAttribute("classEntity", c));
            }
            model.addAttribute("error", e.getMessage());
            return "admin/schedules/bulk-form";
        }
    }

    /**
     * Form chỉnh sửa lịch học
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model,
                          RedirectAttributes redirectAttributes) {
        try {
            Schedule schedule = scheduleService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch học"));

            model.addAttribute("schedule", schedule);
            model.addAttribute("classEntity", schedule.getClassEntity());
            model.addAttribute("statuses", Schedule.ScheduleStatus.values());
            return "admin/schedules/form";
        } catch (Exception e) {
            log.error("Error loading schedule for edit", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/classes";
        }
    }

    /**
     * Xử lý cập nhật lịch học
     */
    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute Schedule schedule,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            if (schedule.getClassEntity() != null && schedule.getClassEntity().getId() != null) {
                classService.findById(schedule.getClassEntity().getId())
                        .ifPresent(c -> model.addAttribute("classEntity", c));
            }
            model.addAttribute("statuses", Schedule.ScheduleStatus.values());
            return "admin/schedules/form";
        }

        try {
            scheduleService.updateSchedule(id, schedule);
            redirectAttributes.addFlashAttribute("success",
                    "Cập nhật lịch học thành công");
            return "redirect:/admin/schedules/class/" + schedule.getClassEntity().getId();
        } catch (Exception e) {
            log.error("Error updating schedule", e);
            if (schedule.getClassEntity() != null && schedule.getClassEntity().getId() != null) {
                classService.findById(schedule.getClassEntity().getId())
                        .ifPresent(c -> model.addAttribute("classEntity", c));
            }
            model.addAttribute("error", e.getMessage());
            model.addAttribute("statuses", Schedule.ScheduleStatus.values());
            return "admin/schedules/form";
        }
    }

    /**
     * Xóa lịch học
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, @RequestParam Long classId,
                        RedirectAttributes redirectAttributes) {
        try {
            scheduleService.deleteSchedule(id);
            redirectAttributes.addFlashAttribute("success", "Xóa lịch học thành công");
        } catch (Exception e) {
            log.error("Error deleting schedule", e);
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi khi xóa lịch học: " + e.getMessage());
        }
        return "redirect:/admin/schedules/class/" + classId;
    }

    /**
     * Đánh dấu lịch hoàn thành
     */
    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id, @RequestParam Long classId,
                          RedirectAttributes redirectAttributes) {
        try {
            scheduleService.completeSchedule(id);
            redirectAttributes.addFlashAttribute("success",
                    "Đánh dấu buổi học hoàn thành");
        } catch (Exception e) {
            log.error("Error completing schedule", e);
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/schedules/class/" + classId;
    }

    /**
     * Hủy lịch học
     */
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, @RequestParam Long classId,
                        RedirectAttributes redirectAttributes) {
        try {
            scheduleService.cancelSchedule(id);
            redirectAttributes.addFlashAttribute("success", "Hủy buổi học thành công");
        } catch (Exception e) {
            log.error("Error cancelling schedule", e);
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/schedules/class/" + classId;
    }
}
