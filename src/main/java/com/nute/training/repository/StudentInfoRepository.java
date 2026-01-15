package com.nute.training.repository;

import com.nute.training.entity.StudentInfo;
import com.nute.training.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentInfoRepository extends JpaRepository<StudentInfo, Long> {
    Optional<StudentInfo> findByUser(User user);
    Optional<StudentInfo> findByStudentCode(String studentCode);
    boolean existsByStudentCodeAndUserIdNot(String studentCode, Long userId);
}
