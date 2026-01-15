package com.nute.training.service;

import com.nute.training.entity.StudentInfo;
import com.nute.training.entity.User;
import com.nute.training.repository.StudentInfoRepository;
import com.nute.training.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service: StudentInfoService
 * Quản lý thông tin chi tiết sinh viên
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudentInfoService {

    private final StudentInfoRepository studentInfoRepository;
    private final UserRepository userRepository;

    /**
     * Tìm thông tin sinh viên theo User ID
     */
    @Transactional(readOnly = true)
    public Optional<StudentInfo> findByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
        return studentInfoRepository.findByUser(user);
    }

    /**
     * Cập nhật hoặc tạo mới thông tin sinh viên
     */
    public StudentInfo saveOrUpdate(Long userId, StudentInfo studentInfoDetails) {
        log.info("Saving/Updating student info for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        if (user.getRole() != User.Role.STUDENT) {
            throw new IllegalArgumentException("Người dùng không phải là sinh viên");
        }

        // Validate student code uniqueness
        if (studentInfoRepository.existsByStudentCodeAndUserIdNot(studentInfoDetails.getStudentCode(), userId)) {
            throw new IllegalArgumentException("Mã sinh viên đã tồn tại trên hệ thống");
        }

        StudentInfo studentInfo = studentInfoRepository.findByUser(user)
                .orElse(new StudentInfo());

        studentInfo.setUser(user);
        studentInfo.setStudentCode(studentInfoDetails.getStudentCode());
        studentInfo.setDateOfBirth(studentInfoDetails.getDateOfBirth());
        studentInfo.setPlaceOfBirth(studentInfoDetails.getPlaceOfBirth());
        studentInfo.setAddress(studentInfoDetails.getAddress());
        studentInfo.setMajor(studentInfoDetails.getMajor());
        studentInfo.setSpecializedClass(studentInfoDetails.getSpecializedClass());

        return studentInfoRepository.save(studentInfo);
    }
    
    /**
     * Kiểm tra xem sinh viên đã cập nhật đủ thông tin chưa
     */
    @Transactional(readOnly = true)
    public boolean isProfileComplete(User user) {
        if (user.getRole() != User.Role.STUDENT) return true;
        return studentInfoRepository.findByUser(user).isPresent();
    }
}
