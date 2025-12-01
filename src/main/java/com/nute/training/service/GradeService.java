package com.nute.training.service;

import com.nute.training.entity.Enrollment;
import com.nute.training.entity.Grade;
import com.nute.training.entity.User;
import com.nute.training.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service: GradeService
 * Quản lý điểm số
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GradeService {

    private final GradeRepository gradeRepository;

    /**
     * Tìm tất cả điểm
     */
    @Transactional(readOnly = true)
    public List<Grade> findAll() {
        return gradeRepository.findAll();
    }

    /**
     * Tìm điểm theo ID
     */
    @Transactional(readOnly = true)
    public Optional<Grade> findById(Long id) {
        return gradeRepository.findById(id);
    }

    /**
     * Tìm điểm theo enrollment
     */
    @Transactional(readOnly = true)
    public Optional<Grade> findByEnrollment(Enrollment enrollment) {
        return gradeRepository.findByEnrollment(enrollment);
    }

    /**
     * Tìm điểm của học viên
     */
    @Transactional(readOnly = true)
    public List<Grade> findGradesByStudent(Long studentId) {
        return gradeRepository.findGradesByStudent(studentId);
    }

    /**
     * Tìm điểm theo lớp
     */
    @Transactional(readOnly = true)
    public List<Grade> findGradesByClass(Long classId) {
        return gradeRepository.findGradesByClass(classId);
    }

    /**
     * Tìm điểm của học viên trong lớp
     */
    @Transactional(readOnly = true)
    public Optional<Grade> findGradeByStudentAndClass(Long studentId, Long classId) {
        return gradeRepository.findGradeByStudentAndClass(studentId, classId);
    }

    /**
     * Tạo hoặc cập nhật điểm
     * Business Rule:
     * - Mỗi enrollment chỉ có 1 bản ghi điểm
     * - Tự động tính điểm tổng kết, xếp loại, đạt/không đạt
     */
    public Grade saveOrUpdateGrade(Enrollment enrollment,
                                    BigDecimal attendanceScore,
                                    BigDecimal processScore,
                                    BigDecimal finalScore,
                                    User gradedBy,
                                    String note) {
        log.info("Saving/updating grade for enrollment ID: {}", enrollment.getId());

        // Validate scores
        validateScore(attendanceScore, "Điểm chuyên cần");
        validateScore(processScore, "Điểm quá trình");
        validateScore(finalScore, "Điểm cuối kỳ");

        // Find existing or create new
        Grade grade = gradeRepository.findByEnrollment(enrollment)
                .orElse(new Grade());

        grade.setEnrollment(enrollment);
        grade.setAttendanceScore(attendanceScore);
        grade.setProcessScore(processScore);
        grade.setFinalScore(finalScore);
        grade.setNote(note);
        grade.setGradedBy(gradedBy);
        grade.setGradedAt(LocalDateTime.now());

        // Calculate all scores automatically
        grade.calculateAll();

        Grade saved = gradeRepository.save(grade);
        log.info("Grade saved successfully with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Cập nhật điểm chuyên cần
     */
    public Grade updateAttendanceScore(Long gradeId, BigDecimal score, User gradedBy) {
        log.info("Updating attendance score for grade ID: {}", gradeId);

        validateScore(score, "Điểm chuyên cần");

        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy điểm với ID: " + gradeId));

        grade.setAttendanceScore(score);
        grade.setGradedBy(gradedBy);
        grade.setGradedAt(LocalDateTime.now());
        grade.calculateAll();

        return gradeRepository.save(grade);
    }

    /**
     * Cập nhật điểm quá trình
     */
    public Grade updateProcessScore(Long gradeId, BigDecimal score, User gradedBy) {
        log.info("Updating process score for grade ID: {}", gradeId);

        validateScore(score, "Điểm quá trình");

        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy điểm với ID: " + gradeId));

        grade.setProcessScore(score);
        grade.setGradedBy(gradedBy);
        grade.setGradedAt(LocalDateTime.now());
        grade.calculateAll();

        return gradeRepository.save(grade);
    }

    /**
     * Cập nhật điểm cuối kỳ
     */
    public Grade updateFinalScore(Long gradeId, BigDecimal score, User gradedBy) {
        log.info("Updating final score for grade ID: {}", gradeId);

        validateScore(score, "Điểm cuối kỳ");

        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy điểm với ID: " + gradeId));

        grade.setFinalScore(score);
        grade.setGradedBy(gradedBy);
        grade.setGradedAt(LocalDateTime.now());
        grade.calculateAll();

        return gradeRepository.save(grade);
    }

    /**
     * Tính lại điểm tổng kết
     */
    public Grade recalculateGrade(Long gradeId) {
        log.info("Recalculating grade for ID: {}", gradeId);

        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy điểm với ID: " + gradeId));

        grade.calculateAll();
        return gradeRepository.save(grade);
    }

    /**
     * Tìm học viên đạt
     */
    @Transactional(readOnly = true)
    public List<Grade> findPassedGradesByClass(Long classId) {
        return gradeRepository.findPassedGradesByClass(classId);
    }

    /**
     * Tìm học viên không đạt
     */
    @Transactional(readOnly = true)
    public List<Grade> findFailedGradesByClass(Long classId) {
        return gradeRepository.findFailedGradesByClass(classId);
    }

    /**
     * Đếm số học viên đạt
     */
    @Transactional(readOnly = true)
    public Long countPassedGrades(Long classId) {
        return gradeRepository.countPassedGradesByClass(classId);
    }

    /**
     * Đếm số học viên không đạt
     */
    @Transactional(readOnly = true)
    public Long countFailedGrades(Long classId) {
        return gradeRepository.countFailedGradesByClass(classId);
    }

    /**
     * Tính điểm trung bình của lớp
     */
    @Transactional(readOnly = true)
    public Double calculateAverageScore(Long classId) {
        Double avg = gradeRepository.calculateAverageScoreByClass(classId);
        return avg != null ? avg : 0.0;
    }

    /**
     * Tính tỷ lệ đạt (%)
     */
    @Transactional(readOnly = true)
    public Double calculatePassRate(Long classId) {
        Double rate = gradeRepository.calculatePassRateByClass(classId);
        return rate != null ? rate : 0.0;
    }

    /**
     * Tìm top học viên
     */
    @Transactional(readOnly = true)
    public List<Grade> findTopGrades(Long classId, int limit) {
        List<Grade> allGrades = gradeRepository.findTopGradesByClass(classId);
        return allGrades.stream().limit(limit).toList();
    }

    /**
     * Xóa điểm
     */
    public void deleteGrade(Long id) {
        log.info("Deleting grade ID: {}", id);

        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy điểm với ID: " + id));

        gradeRepository.delete(grade);
        log.info("Grade deleted successfully: {}", id);
    }

    /**
     * Validate điểm (0-10)
     */
    private void validateScore(BigDecimal score, String scoreName) {
        if (score != null) {
            if (score.compareTo(BigDecimal.ZERO) < 0 ||
                score.compareTo(new BigDecimal("10")) > 0) {
                throw new IllegalArgumentException(
                        scoreName + " phải trong khoảng 0-10");
            }
        }
    }
}
