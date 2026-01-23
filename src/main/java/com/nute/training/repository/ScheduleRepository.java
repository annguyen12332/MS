package com.nute.training.repository;

import com.nute.training.entity.ClassEntity;
import com.nute.training.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository: ScheduleRepository
 * Quản lý thời khóa biểu
 */
@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    /**
     * Tìm lịch theo lớp
     */
    List<Schedule> findByClassEntity(ClassEntity classEntity);

    /**
     * Tìm lịch theo lớp, sắp xếp theo buổi học
     */
    List<Schedule> findByClassEntityOrderBySessionNumberAsc(ClassEntity classEntity);

    /**
     * Tìm lịch theo lớp và buổi học
     */
    Optional<Schedule> findByClassEntityAndSessionNumber(
            ClassEntity classEntity,
            Integer sessionNumber
    );

    /**
     * Tìm lịch theo trạng thái
     */
    List<Schedule> findByStatus(Schedule.ScheduleStatus status);

    /**
     * Tìm lịch theo lớp và trạng thái
     */
    List<Schedule> findByClassEntityAndStatus(
            ClassEntity classEntity,
            Schedule.ScheduleStatus status
    );

    /**
     * Tìm lịch theo ngày
     */
    List<Schedule> findBySessionDate(LocalDate sessionDate);

    /**
     * Tìm lịch theo khoảng thời gian
     */
    @Query("SELECT s FROM Schedule s WHERE " +
           "s.sessionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY s.sessionDate, s.startTime")
    List<Schedule> findSchedulesByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Tìm lịch của giảng viên theo ngày
     */
    @Query("SELECT s FROM Schedule s WHERE " +
           "s.classEntity.teacher.id = :teacherId AND s.sessionDate = :date " +
           "ORDER BY s.startTime")
    List<Schedule> findTeacherScheduleByDate(
            @Param("teacherId") Long teacherId,
            @Param("date") LocalDate date
    );

    /**
     * Tìm lịch của giảng viên theo khoảng thời gian
     */
    @Query("SELECT s FROM Schedule s WHERE " +
           "s.classEntity.teacher.id = :teacherId AND " +
           "s.sessionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY s.sessionDate, s.startTime")
    List<Schedule> findTeacherScheduleByDateRange(
            @Param("teacherId") Long teacherId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Tìm lịch sắp tới của lớp (từ hôm nay trở đi)
     */
    @Query("SELECT s FROM Schedule s WHERE " +
           "s.classEntity = :classEntity AND s.sessionDate >= :today AND s.status = 'SCHEDULED' " +
           "ORDER BY s.sessionDate, s.startTime")
    List<Schedule> findUpcomingSchedules(
            @Param("classEntity") ClassEntity classEntity,
            @Param("today") LocalDate today
    );

    /**
     * Tìm lịch sắp tới của giảng viên
     */
    @Query("SELECT s FROM Schedule s WHERE " +
           "s.classEntity.teacher.id = :teacherId AND s.sessionDate >= :today AND s.status != 'CANCELLED' " +
           "ORDER BY s.sessionDate, s.startTime")
    List<Schedule> findUpcomingSchedulesForTeacher(
            @Param("teacherId") Long teacherId,
            @Param("today") LocalDate today
    );

    /**
     * Tìm lịch sắp tới của học viên (dựa trên các lớp đã đăng ký APPROVED)
     * Eager fetch classEntity và course để tránh N+1 problem
     */
    @Query("SELECT DISTINCT s FROM Schedule s " +
           "JOIN FETCH s.classEntity c " +
           "JOIN FETCH c.course " +
           "JOIN c.enrollments e " +
           "WHERE e.student.id = :studentId " +
           "AND e.status = 'APPROVED' " +
           "AND s.sessionDate >= :today " +
           "AND s.status != 'CANCELLED' " +
           "ORDER BY s.sessionDate, s.startTime")
    List<Schedule> findUpcomingSchedulesForStudent(
            @Param("studentId") Long studentId,
            @Param("today") LocalDate today
    );

    /**
     * Tìm lịch của học viên theo khoảng thời gian
     */
    @Query("SELECT s FROM Schedule s JOIN s.classEntity c JOIN c.enrollments e WHERE " +
           "e.student.id = :studentId AND e.status = 'APPROVED' AND " +
           "s.sessionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY s.sessionDate, s.startTime")
    List<Schedule> findStudentScheduleByDateRange(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Đếm số buổi đã hoàn thành của lớp
     */
    @Query("SELECT COUNT(s) FROM Schedule s WHERE " +
           "s.classEntity = :classEntity AND s.status = 'COMPLETED'")
    Long countCompletedSessionsByClass(@Param("classEntity") ClassEntity classEntity);

    /**
     * Kiểm tra xung đột lịch (cùng phòng, cùng ngày, giờ trùng nhau)
     */
    @Query("SELECT s FROM Schedule s WHERE " +
           "s.room = :room AND s.sessionDate = :date AND s.status != 'CANCELLED' AND " +
           "((s.startTime <= :startTime AND s.endTime > :startTime) OR " +
           "(s.startTime < :endTime AND s.endTime >= :endTime) OR " +
           "(s.startTime >= :startTime AND s.endTime <= :endTime))")
    List<Schedule> findConflictingSchedules(
            @Param("room") String room,
            @Param("date") LocalDate date,
            @Param("startTime") java.time.LocalTime startTime,
            @Param("endTime") java.time.LocalTime endTime
    );

    /**
     * Tìm số buổi lớn nhất hiện tại của lớp
     */
    @Query("SELECT MAX(s.sessionNumber) FROM Schedule s WHERE s.classEntity = :classEntity")
    Integer findMaxSessionNumberByClass(@Param("classEntity") ClassEntity classEntity);
}
