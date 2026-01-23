package com.nute.training.service;

import com.nute.training.entity.ClassEntity;
import com.nute.training.entity.Enrollment;
import com.nute.training.entity.Grade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GradeExportService {

    public byte[] exportClassGrades(ClassEntity classEntity, List<Enrollment> enrollments, List<Grade> grades) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Bảng điểm");

            // Styles
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle centerStyle = workbook.createCellStyle();
            centerStyle.cloneStyleFrom(dataStyle);
            centerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Class Info Rows
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BẢNG ĐIỂM LỚP HỌC: " + classEntity.getClassName().toUpperCase());
            // Could merge cells here if desired

            Row infoRow = sheet.createRow(1);
            infoRow.createCell(0).setCellValue("Mã lớp: " + classEntity.getClassCode());
            infoRow.createCell(3).setCellValue("Giảng viên: " + (classEntity.getTeacher() != null ? classEntity.getTeacher().getFullName() : "N/A"));

            // Header Row
            String[] headers = {"STT", "Mã SV", "Họ và tên", "Email", "Điểm CC (10%)", "Điểm QT (30%)", "Điểm CK (60%)", "Tổng kết", "Xếp loại", "Ghi chú"};
            Row headerRow = sheet.createRow(3);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Map grades by student ID for quick lookup
            Map<Long, Grade> gradeMap = grades.stream()
                    .filter(g -> g.getEnrollment() != null && g.getEnrollment().getStudent() != null)
                    .collect(Collectors.toMap(
                            g -> g.getEnrollment().getStudent().getId(),
                            Function.identity(),
                            (existing, replacement) -> existing
                    ));

            // Data Rows
            int rowIdx = 4;
            int stt = 1;
            for (Enrollment enrollment : enrollments) {
                if (enrollment.getStudent() == null) continue;

                Row row = sheet.createRow(rowIdx++);
                Grade grade = gradeMap.get(enrollment.getStudent().getId());

                // STT
                createCell(row, 0, String.valueOf(stt++), centerStyle);
                // Mã SV
                createCell(row, 1, enrollment.getStudent().getUsername(), dataStyle);
                // Họ tên
                createCell(row, 2, enrollment.getStudent().getFullName(), dataStyle);
                // Email
                createCell(row, 3, enrollment.getStudent().getEmail(), dataStyle);

                if (grade != null) {
                    createCell(row, 4, grade.getAttendanceScore() != null ? grade.getAttendanceScore().toString() : "", centerStyle);
                    createCell(row, 5, grade.getProcessScore() != null ? grade.getProcessScore().toString() : "", centerStyle);
                    createCell(row, 6, grade.getFinalScore() != null ? grade.getFinalScore().toString() : "", centerStyle);
                    createCell(row, 7, grade.getTotalScore() != null ? grade.getTotalScore().toString() : "", centerStyle);
                    createCell(row, 8, grade.getGradeLetter() != null ? grade.getGradeLetter() : "", centerStyle);
                    createCell(row, 9, grade.getNote() != null ? grade.getNote() : "", dataStyle);
                } else {
                    for (int i = 4; i <= 9; i++) {
                        createCell(row, i, "", dataStyle);
                    }
                }
            }

            // Auto size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}
