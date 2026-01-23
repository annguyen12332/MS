package com.nute.training.service;

import com.nute.training.entity.ClassEntity;
import com.nute.training.entity.Schedule;
import com.nute.training.entity.User;
import com.nute.training.repository.ClassRepository;
import com.nute.training.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Service: ScheduleService
 * Quản lý thời khóa biểu
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ClassRepository classRepository;

    /**
     * Tìm tất cả lịch học
     */
    @Transactional(readOnly = true)
    public List<Schedule> findAll() {
        return scheduleRepository.findAll();
    }

    /**
     * Tìm lịch theo ID
     */
    @Transactional(readOnly = true)
    public Optional<Schedule> findById(Long id) {
        return scheduleRepository.findById(id);
    }

    /**
     * Tìm lịch theo lớp
     */
    @Transactional(readOnly = true)
    public List<Schedule> findByClass(ClassEntity classEntity) {
        return scheduleRepository.findByClassEntityOrderBySessionNumberAsc(classEntity);
    }

    /**
     * Tìm lịch theo ngày
     */
    @Transactional(readOnly = true)
    public List<Schedule> findByDate(LocalDate date) {
        return scheduleRepository.findBySessionDate(date);
    }

    /**
     * Tìm lịch của giảng viên theo ngày
     */
    @Transactional(readOnly = true)
    public List<Schedule> findTeacherScheduleByDate(Long teacherId, LocalDate date) {
        return scheduleRepository.findTeacherScheduleByDate(teacherId, date);
    }

    /**
     * Tìm lịch của giảng viên theo khoảng thời gian
     */
    @Transactional(readOnly = true)
    public List<Schedule> findTeacherScheduleByDateRange(User teacher, LocalDate startDate, LocalDate endDate) {
        return scheduleRepository.findTeacherScheduleByDateRange(teacher.getId(), startDate, endDate);
    }

    /**
     * Tìm lịch của học viên theo khoảng thời gian
     */
    @Transactional(readOnly = true)
    public List<Schedule> findStudentScheduleByDateRange(User student, LocalDate startDate, LocalDate endDate) {
        return scheduleRepository.findStudentScheduleByDateRange(student.getId(), startDate, endDate);
    }

    /**
     * Tìm lịch sắp tới của lớp
     */
    @Transactional(readOnly = true)
    public List<Schedule> findUpcomingSchedules(ClassEntity classEntity) {
        return scheduleRepository.findUpcomingSchedules(classEntity, LocalDate.now());
    }

    /**
     * Tìm lịch sắp tới của giảng viên
     */
    @Transactional(readOnly = true)
    public List<Schedule> findUpcomingSchedulesForTeacher(User teacher) {
        return scheduleRepository.findUpcomingSchedulesForTeacher(teacher.getId(), LocalDate.now());
    }

    /**
     * Tìm lịch sắp tới của học viên
     */
    @Transactional(readOnly = true)
    public List<Schedule> findUpcomingSchedulesForStudent(User student) {
        return scheduleRepository.findUpcomingSchedulesForStudent(student.getId(), LocalDate.now());
    }

    /**
     * Tạo lịch học mới
     * Business Rule:
     */
    public Schedule createSchedule(Schedule schedule) {
        log.info("Creating schedule for class: {}, session: {}",
                schedule.getClassEntity().getClassCode(), schedule.getSessionNumber());

        // Check duplicate session number in class
        if (scheduleRepository.findByClassEntityAndSessionNumber(
                schedule.getClassEntity(), schedule.getSessionNumber()).isPresent()) {
            throw new IllegalArgumentException(
                    "Buổi học số " + schedule.getSessionNumber() + " đã tồn tại trong lớp này");
        }

        // Validate time range
        validateTimeRange(schedule.getStartTime(), schedule.getEndTime());

        // Check room conflict
        if (schedule.getRoom() != null) {
            List<Schedule> conflicts = scheduleRepository.findConflictingSchedules(
                    schedule.getRoom(),
                    schedule.getSessionDate(),
                    schedule.getStartTime(),
                    schedule.getEndTime()
            );

            if (!conflicts.isEmpty()) {
                throw new IllegalStateException(
                        "Phòng " + schedule.getRoom() + " đã có lịch trùng vào thời gian này");
            }
        }

        // Set default status
        if (schedule.getStatus() == null) {
            schedule.setStatus(Schedule.ScheduleStatus.SCHEDULED);
        }

        Schedule saved = scheduleRepository.save(schedule);
        log.info("Schedule created successfully with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Tạo lịch học hàng loạt
     */
    public List<Schedule> generateBatchSchedules(com.nute.training.dto.BulkScheduleCreateDto dto) {
        log.info("Generating batch schedules for class ID: {}", dto.getClassId());

        ClassEntity classEntity = classRepository.findById(dto.getClassId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lớp học"));

        if (classEntity.getStartDate() == null || classEntity.getEndDate() == null) {
            throw new IllegalArgumentException("Lớp học chưa có ngày bắt đầu hoặc kết thúc");
        }

        Integer currentMaxSession = scheduleRepository.findMaxSessionNumberByClass(classEntity);
        int nextSessionNumber = (currentMaxSession != null) ? currentMaxSession + 1 : 1;

        List<Schedule> createdSchedules = new java.util.ArrayList<>();
        LocalDate currentDate = classEntity.getStartDate();

        while (!currentDate.isAfter(classEntity.getEndDate())) {
            // Check if current date matches selected days
            // java.time.DayOfWeek value: 1 (Mon) -> 7 (Sun)
            if (dto.getDaysOfWeek() != null && dto.getDaysOfWeek().contains(currentDate.getDayOfWeek().getValue())) {
                Schedule schedule = new Schedule();
                schedule.setClassEntity(classEntity);
                schedule.setSessionNumber(nextSessionNumber++);
                schedule.setSessionDate(currentDate);
                schedule.setStartTime(dto.getStartTime());
                schedule.setEndTime(dto.getEndTime());
                schedule.setRoom(dto.getRoom() != null && !dto.getRoom().isEmpty() ? dto.getRoom() : classEntity.getRoom());
                schedule.setStatus(Schedule.ScheduleStatus.SCHEDULED);

                // Create schedule (reuses validation logic)
                // Note: createSchedule throws RuntimeException on conflict, which will rollback transaction
                createdSchedules.add(createSchedule(schedule));
            }
            currentDate = currentDate.plusDays(1);
        }

        if (createdSchedules.isEmpty()) {
            throw new IllegalArgumentException("Không có lịch học nào được tạo. Vui lòng kiểm tra ngày bắt đầu/kết thúc và các ngày trong tuần đã chọn.");
        }
        
        return createdSchedules;
    }

    /**
     * Cập nhật lịch học
     */
    public Schedule updateSchedule(Long id, Schedule scheduleDetails) {
        log.info("Updating schedule ID: {}", id);

        Schedule existing = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy lịch học với ID: " + id));

        // Validate time range
        validateTimeRange(scheduleDetails.getStartTime(), scheduleDetails.getEndTime());

        // Check room conflict (if room or time changed)
        if (scheduleDetails.getRoom() != null &&
            (!scheduleDetails.getRoom().equals(existing.getRoom()) ||
             !scheduleDetails.getSessionDate().equals(existing.getSessionDate()) ||
             !scheduleDetails.getStartTime().equals(existing.getStartTime()) ||
             !scheduleDetails.getEndTime().equals(existing.getEndTime()))) {

            List<Schedule> conflicts = scheduleRepository.findConflictingSchedules(
                    scheduleDetails.getRoom(),
                    scheduleDetails.getSessionDate(),
                    scheduleDetails.getStartTime(),
                    scheduleDetails.getEndTime()
            );

            // Exclude current schedule from conflicts
            conflicts.removeIf(s -> s.getId().equals(id));

            if (!conflicts.isEmpty()) {
                throw new IllegalStateException(
                        "Phòng " + scheduleDetails.getRoom() + " đã có lịch trùng vào thời gian này");
            }
        }

        // Update fields
        existing.setSessionDate(scheduleDetails.getSessionDate());
        existing.setStartTime(scheduleDetails.getStartTime());
        existing.setEndTime(scheduleDetails.getEndTime());
        existing.setRoom(scheduleDetails.getRoom());
        existing.setTopic(scheduleDetails.getTopic());
        existing.setDescription(scheduleDetails.getDescription());
        existing.setStatus(scheduleDetails.getStatus());

        Schedule updated = scheduleRepository.save(existing);
        log.info("Schedule updated successfully: {}", updated.getId());
        return updated;
    }

    /**
     * Đánh dấu lịch hoàn thành
     */
    public void completeSchedule(Long scheduleId) {
        log.info("Completing schedule ID: {}", scheduleId);

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy lịch học với ID: " + scheduleId));

        schedule.setStatus(Schedule.ScheduleStatus.COMPLETED);
        scheduleRepository.save(schedule);

        log.info("Schedule completed successfully: {}", scheduleId);
    }

    /**
     * Hủy lịch học
     */
    public void cancelSchedule(Long scheduleId) {
        log.info("Cancelling schedule ID: {}", scheduleId);

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy lịch học với ID: " + scheduleId));

        schedule.setStatus(Schedule.ScheduleStatus.CANCELLED);
        scheduleRepository.save(schedule);

        log.info("Schedule cancelled successfully: {}", scheduleId);
    }

    /**
     * Xóa lịch học
     */
    public void deleteSchedule(Long id) {
        log.info("Deleting schedule ID: {}", id);

        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy lịch học với ID: " + id));

        scheduleRepository.delete(schedule);
        log.info("Schedule deleted successfully: {}", id);
    }

    /**
     * Validate time range
     */
    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime != null && endTime != null) {
            if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
                throw new IllegalArgumentException(
                        "Giờ kết thúc phải sau giờ bắt đầu");
            }
        }
    }

    /**
     * Đếm số buổi đã hoàn thành
     */
    @Transactional(readOnly = true)
    public Long countCompletedSessionsByClass(ClassEntity classEntity) {
        return scheduleRepository.countCompletedSessionsByClass(classEntity);
    }
}
