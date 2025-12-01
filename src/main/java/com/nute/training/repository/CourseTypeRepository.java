package com.nute.training.repository;

import com.nute.training.entity.CourseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository: CourseTypeRepository
 * Quản lý loại khóa học
 */
@Repository
public interface CourseTypeRepository extends JpaRepository<CourseType, Long> {

    /**
     * Tìm loại khóa học theo code
     */
    Optional<CourseType> findByCode(String code);

    /**
     * Kiểm tra code đã tồn tại chưa
     */
    boolean existsByCode(String code);
}
