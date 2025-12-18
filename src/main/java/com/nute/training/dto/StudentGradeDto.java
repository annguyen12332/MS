package com.nute.training.dto;

import com.nute.training.entity.Enrollment;
import com.nute.training.entity.Grade;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO: StudentGradeDto
 * Kết hợp thông tin Enrollment và Grade để hiển thị điểm học sinh
 * Hỗ trợ cả trường hợp chưa có điểm (grade = null)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentGradeDto {

    private Enrollment enrollment;
    private Grade grade; // Có thể null nếu chưa có điểm

    /**
     * Kiểm tra đã có điểm chưa
     */
    public boolean hasGrade() {
        return grade != null;
    }

    /**
     * Lấy tên lớp học
     */
    public String getClassName() {
        return enrollment.getClassEntity().getClassName();
    }

    /**
     * Lấy tên khóa học
     */
    public String getCourseName() {
        return enrollment.getClassEntity().getCourse().getName();
    }

    /**
     * Lấy mã khóa học
     */
    public String getCourseCode() {
        return enrollment.getClassEntity().getCourse().getCode();
    }

    /**
     * Lấy tên giáo viên
     */
    public String getTeacherName() {
        if (enrollment.getClassEntity().getTeacher() != null) {
            return enrollment.getClassEntity().getTeacher().getFullName();
        }
        return "Chưa phân công";
    }

    /**
     * Lấy điểm chuyên cần (attendance)
     * Trả về 0 nếu chưa có điểm
     */
    public BigDecimal getAttendanceScore() {
        return hasGrade() ? grade.getAttendanceScore() : BigDecimal.ZERO;
    }

    /**
     * Lấy điểm quá trình (process)
     * Trả về 0 nếu chưa có điểm
     */
    public BigDecimal getProcessScore() {
        return hasGrade() ? grade.getProcessScore() : BigDecimal.ZERO;
    }

    /**
     * Lấy điểm cuối kỳ (final)
     * Trả về 0 nếu chưa có điểm
     */
    public BigDecimal getFinalScore() {
        return hasGrade() ? grade.getFinalScore() : BigDecimal.ZERO;
    }

    /**
     * Lấy tổng điểm
     * Trả về 0 nếu chưa có điểm
     */
    public BigDecimal getTotalScore() {
        return hasGrade() ? grade.getTotalScore() : BigDecimal.ZERO;
    }

    /**
     * Lấy xếp loại (A, B, C, D, F)
     * Trả về "N/A" nếu chưa có điểm
     */
    public String getGradeLetter() {
        return hasGrade() ? grade.getGradeLetter() : "N/A";
    }

    /**
     * Kiểm tra đạt hay không
     * Trả về false nếu chưa có điểm
     */
    public Boolean isPass() {
        return hasGrade() ? grade.getPass() : false;
    }

    /**
     * Alias cho isPass() để Thymeleaf có thể truy cập qua item.pass
     */
    public Boolean getPass() {
        return isPass();
    }

    /**
     * Lấy trạng thái enrollment
     */
    public Enrollment.EnrollmentStatus getEnrollmentStatus() {
        return enrollment.getStatus();
    }
}
