package com.nute.training.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
public class BulkScheduleCreateDto {
    @NotNull(message = "Lớp học không được để trống")
    private Long classId;

    @NotNull(message = "Phải chọn ít nhất một ngày trong tuần")
    private List<Integer> daysOfWeek; // 1 = Monday, ..., 7 = Sunday

    @NotNull(message = "Giờ bắt đầu không được để trống")
    private LocalTime startTime;

    @NotNull(message = "Giờ kết thúc không được để trống")
    private LocalTime endTime;

    private String room;
}
