package com.nute.training.repository;

import com.nute.training.entity.Attendance;
import com.nute.training.entity.Schedule;
import com.nute.training.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository: AttendanceRepository
 * Quản lý điểm danh
 */
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    /**
     * Tìm điểm danh theo buổi học và học viên
     */
    Optional<Attendance> findByScheduleAndStudent(Schedule schedule, User student);

    /**
     * Kiểm tra học viên đã được điểm danh trong buổi học chưa
     */
    boolean existsByScheduleAndStudent(Schedule schedule, User student);

    /**
     * Tìm tất cả điểm danh của buổi học
     */
    List<Attendance> findBySchedule(Schedule schedule);

    /**
     * Tìm tất cả điểm danh của học viên
     */
    List<Attendance> findByStudent(User student);

    /**
     * Tìm điểm danh theo trạng thái
     */
    List<Attendance> findByStatus(Attendance.AttendanceStatus status);

    /**
     * Tìm điểm danh của học viên theo lớp
     */
    @Query("SELECT a FROM Attendance a WHERE " +
           "a.student = :student AND a.schedule.classEntity.id = :classId " +
           "ORDER BY a.schedule.sessionNumber")
    List<Attendance> findStudentAttendanceByClass(
            @Param("student") User student,
            @Param("classId") Long classId
    );

    /**
     * Đếm số buổi học viên có mặt (PRESENT hoặc LATE)
     */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE " +
           "a.student.id = :studentId AND a.schedule.classEntity.id = :classId AND " +
           "a.status IN ('PRESENT', 'LATE')")
    Long countPresentAttendances(
            @Param("studentId") Long studentId,
            @Param("classId") Long classId
    );

    /**
     * Đếm số buổi học viên vắng
     */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE " +
           "a.student.id = :studentId AND a.schedule.classEntity.id = :classId AND " +
           "a.status = 'ABSENT'")
    Long countAbsentAttendances(
            @Param("studentId") Long studentId,
            @Param("classId") Long classId
    );

    /**
     * Tính tỷ lệ điểm danh của học viên trong lớp (%)
     */
    @Query("SELECT " +
           "CAST(COUNT(CASE WHEN a.status IN ('PRESENT', 'LATE') THEN 1 END) AS double) / " +
           "CAST(COUNT(a) AS double) * 100 " +
           "FROM Attendance a WHERE " +
           "a.student.id = :studentId AND a.schedule.classEntity.id = :classId")
    Double calculateAttendanceRate(
            @Param("studentId") Long studentId,
            @Param("classId") Long classId
    );

    /**
     * Thống kê điểm danh theo trạng thái trong buổi học
     */
    @Query("SELECT a.status, COUNT(a) FROM Attendance a WHERE " +
           "a.schedule = :schedule GROUP BY a.status")
    List<Object[]> countAttendancesByStatusInSchedule(@Param("schedule") Schedule schedule);

    /**
     * Tìm học viên chưa được điểm danh trong buổi học
     */
    @Query("SELECT e.student FROM Enrollment e WHERE " +
           "e.classEntity = :classEntity AND e.status = 'APPROVED' AND " +
           "e.student NOT IN (SELECT a.student FROM Attendance a WHERE a.schedule = :schedule)")
    List<User> findStudentsNotMarkedInSchedule(
            @Param("classEntity") com.nute.training.entity.ClassEntity classEntity,
            @Param("schedule") Schedule schedule
    );
}
