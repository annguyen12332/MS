package com.nute.training.controller.student;

import com.nute.training.entity.User;
import com.nute.training.exception.UnauthorizedException;
import com.nute.training.service.ScheduleService;
import com.nute.training.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/**
 * Controller: StudentScheduleController
 * Học viên xem lịch học
 */
@Controller
@RequestMapping("/student/schedule")
@RequiredArgsConstructor
@Slf4j
public class StudentScheduleController {

    private final ScheduleService scheduleService;
    private final AuthenticationHelper authenticationHelper;

    /**
     * Xem lịch học theo lịch (calendar view)
     */
    @GetMapping
    public String viewSchedule(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        User currentStudent = authenticationHelper.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException());

        // Default to current month if no dates provided
        if (startDate == null || endDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
            endDate = startDate.plusMonths(1).minusDays(1);
        }

        var schedules = scheduleService.findStudentScheduleByDateRange(currentStudent, startDate, endDate);

        model.addAttribute("schedules", schedules);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("pageTitle", "Lịch học của tôi");

        return "student/schedule/calendar";
    }
}
