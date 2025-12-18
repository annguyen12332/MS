package com.nute.training.controller.student;

import com.nute.training.entity.Grade;
import com.nute.training.entity.User;
import com.nute.training.service.GradeService;
import com.nute.training.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Controller: StudentGradeController
 * Học viên xem điểm
 */
@Controller
@RequestMapping("/student/grades")
@RequiredArgsConstructor
@Slf4j
public class StudentGradeController {

    private final GradeService gradeService;
    private final AuthenticationHelper authenticationHelper;

    @GetMapping
    public String myGrades(Model model) {
        try {
            User currentStudent = authenticationHelper.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Grade> grades = gradeService.findGradesByStudent(currentStudent.getId());
            model.addAttribute("grades", grades);
            model.addAttribute("pageTitle", "Điểm số");

            return "student/grades/list";
        } catch (Exception e) {
            log.error("Error loading student grades", e);
            return "redirect:/student/dashboard";
        }
    }
}
