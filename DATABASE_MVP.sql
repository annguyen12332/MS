-- =====================================================
-- DATABASE: short_term_training (MVP Version)
-- Description: Hệ thống quản lý đào tạo ngắn hạn - TỐI GIẢN
-- Tech Stack: Spring Boot 3.x + MySQL 8.0+
-- =====================================================

CREATE DATABASE IF NOT EXISTS short_term_training
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE short_term_training;

-- =====================================================
-- 1. USERS - Người dùng (Admin, Teacher, Student)
-- =====================================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,  -- BCrypt hashed
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role ENUM('ADMIN', 'TEACHER', 'STUDENT') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE',
    avatar VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_role (role),
    INDEX idx_status (status),
    INDEX idx_email (email)
) ENGINE=InnoDB;

-- =====================================================
-- 2. COURSE_TYPES - Loại khóa học
-- =====================================================
CREATE TABLE course_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_code (code)
) ENGINE=InnoDB;

-- =====================================================
-- 3. COURSES - Khóa học
-- =====================================================
CREATE TABLE courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_type_id BIGINT,
    code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,  -- Nội dung đào tạo
    duration_hours INT,  -- Thời lượng (giờ)
    duration_sessions INT,  -- Số buổi học
    tuition_fee DECIMAL(10,2),  -- Học phí
    max_students INT,  -- Sĩ số tối đa
    requirements TEXT,
    status ENUM('DRAFT', 'ACTIVE', 'INACTIVE') DEFAULT 'DRAFT',
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (course_type_id) REFERENCES course_types(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,

    INDEX idx_code (code),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- =====================================================
-- 4. CLASSES - Lớp học
-- =====================================================
CREATE TABLE classes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    teacher_id BIGINT,  -- Giảng viên phụ trách
    class_code VARCHAR(20) UNIQUE NOT NULL,
    class_name VARCHAR(200),
    start_date DATE,  -- Thời gian tổ chức
    end_date DATE,
    max_students INT,
    current_students INT DEFAULT 0,
    room VARCHAR(50),  -- Phòng học mặc định
    status ENUM('PENDING', 'ONGOING', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE SET NULL,

    INDEX idx_class_code (class_code),
    INDEX idx_status (status),
    INDEX idx_dates (start_date, end_date)
) ENGINE=InnoDB;

-- =====================================================
-- 5. ENROLLMENTS - Đăng ký học
-- =====================================================
CREATE TABLE enrollments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    enrollment_date DATE NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED', 'DROPPED') DEFAULT 'PENDING',
    payment_status ENUM('UNPAID', 'PARTIAL', 'PAID') DEFAULT 'UNPAID',
    payment_amount DECIMAL(10,2) DEFAULT 0,
    notes TEXT,
    approved_by BIGINT,  -- Admin duyệt
    approved_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,

    UNIQUE KEY unique_enrollment (student_id, class_id),
    INDEX idx_status (status),
    INDEX idx_payment_status (payment_status)
) ENGINE=InnoDB;

-- =====================================================
-- 6. SCHEDULES - Thời khóa biểu
-- =====================================================
CREATE TABLE schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id BIGINT NOT NULL,
    session_number INT NOT NULL,  -- Buổi học thứ mấy
    session_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    room VARCHAR(50),
    topic VARCHAR(200),  -- Nội dung buổi học
    description TEXT,
    status ENUM('SCHEDULED', 'COMPLETED', 'CANCELLED') DEFAULT 'SCHEDULED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,

    UNIQUE KEY unique_session (class_id, session_number),
    INDEX idx_date (session_date),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- =====================================================
-- 7. ATTENDANCES - Điểm danh
-- =====================================================
CREATE TABLE attendances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    status ENUM('PRESENT', 'ABSENT', 'LATE', 'EXCUSED') NOT NULL,
    note TEXT,
    marked_by BIGINT,  -- Giảng viên điểm danh
    marked_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (marked_by) REFERENCES users(id) ON DELETE SET NULL,

    UNIQUE KEY unique_attendance (schedule_id, student_id),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- =====================================================
-- 8. GRADES - Điểm số
-- =====================================================
CREATE TABLE grades (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id BIGINT NOT NULL UNIQUE,
    attendance_score DECIMAL(5,2),  -- Điểm chuyên cần (0-10)
    process_score DECIMAL(5,2),     -- Điểm quá trình (0-10)
    final_score DECIMAL(5,2),       -- Điểm thi cuối kỳ (0-10)
    total_score DECIMAL(5,2),       -- Điểm tổng kết (0-10)
    grade_letter VARCHAR(5),        -- A, B, C, D, F hoặc Đạt/Không đạt
    pass BOOLEAN DEFAULT FALSE,     -- Đạt/Không đạt
    note TEXT,
    graded_by BIGINT,               -- Giảng viên chấm
    graded_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (enrollment_id) REFERENCES enrollments(id) ON DELETE CASCADE,
    FOREIGN KEY (graded_by) REFERENCES users(id) ON DELETE SET NULL,

    INDEX idx_pass (pass)
) ENGINE=InnoDB;

-- =====================================================
-- 9. CERTIFICATES - Chứng chỉ
-- =====================================================
CREATE TABLE certificates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id BIGINT NOT NULL,
    certificate_code VARCHAR(50) UNIQUE NOT NULL,
    issue_date DATE NOT NULL,
    file_path VARCHAR(255),  -- Đường dẫn file PDF (nếu có)
    status ENUM('DRAFT', 'ISSUED', 'REVOKED') DEFAULT 'DRAFT',
    issued_by BIGINT,  -- Admin cấp
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (enrollment_id) REFERENCES enrollments(id) ON DELETE CASCADE,
    FOREIGN KEY (issued_by) REFERENCES users(id) ON DELETE SET NULL,

    INDEX idx_code (certificate_code),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- =====================================================
-- SEED DATA - Dữ liệu mẫu
-- =====================================================

-- Insert Course Types
INSERT INTO course_types (name, code, description) VALUES
('Ứng dụng Công nghệ Thông tin', 'CNTT', 'Các khóa học về CNTT đạt chuẩn đầu ra đại học'),
('Ngoại ngữ', 'NN', 'Các khóa học ngoại ngữ (Anh, Trung, Nhật, Hàn)'),
('Kỹ năng mềm', 'KNM', 'Các khóa học kỹ năng mềm'),
('Nghiệp vụ Sư phạm', 'SP', 'Bồi dưỡng chứng chỉ nghiệp vụ sư phạm');

-- Insert Users (Password: 123456 - BCrypt hash)
-- Admin
INSERT INTO users (username, email, password, full_name, role) VALUES
('admin', 'admin@nute.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Quản trị viên', 'ADMIN');

-- Teachers
INSERT INTO users (username, email, password, full_name, phone, role) VALUES
('teacher1', 'teacher1@nute.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Nguyễn Văn A', '0987654321', 'TEACHER'),
('teacher2', 'teacher2@nute.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Trần Thị B', '0987654322', 'TEACHER');

-- Students
INSERT INTO users (username, email, password, full_name, phone, role) VALUES
('student1', 'student1@nute.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Lê Văn C', '0912345678', 'STUDENT'),
('student2', 'student2@nute.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Phạm Thị D', '0912345679', 'STUDENT'),
('student3', 'student3@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Hoàng Văn E', '0912345680', 'STUDENT');

-- Insert Sample Courses
INSERT INTO courses (course_type_id, code, name, description, duration_hours, duration_sessions, tuition_fee, max_students, status, created_by) VALUES
(1, 'CNTT001', 'Tin học văn phòng', 'Khóa học Word, Excel, PowerPoint cơ bản đến nâng cao', 45, 15, 1500000, 30, 'ACTIVE', 1),
(1, 'CNTT002', 'Lập trình Java cơ bản', 'Khóa học lập trình Java từ cơ bản đến nâng cao', 60, 20, 2000000, 25, 'ACTIVE', 1),
(2, 'NN001', 'Tiếng Anh giao tiếp A1', 'Khóa học tiếng Anh giao tiếp cơ bản', 60, 20, 1800000, 30, 'ACTIVE', 1),
(3, 'KNM001', 'Kỹ năng làm việc nhóm', 'Khóa học kỹ năng làm việc nhóm hiệu quả', 30, 10, 1000000, 40, 'ACTIVE', 1),
(4, 'SP001', 'Nghiệp vụ sư phạm', 'Bồi dưỡng chứng chỉ nghiệp vụ sư phạm', 90, 30, 3000000, 50, 'ACTIVE', 1);

-- Insert Sample Classes
INSERT INTO classes (course_id, teacher_id, class_code, class_name, start_date, end_date, max_students, room, status) VALUES
(1, 2, 'CNTT001-2024-01', 'Tin học văn phòng - Lớp 1', '2024-03-01', '2024-05-15', 30, 'B101', 'ONGOING'),
(2, 2, 'CNTT002-2024-01', 'Java cơ bản - Lớp 1', '2024-03-15', '2024-06-30', 25, 'B202', 'ONGOING'),
(3, 3, 'NN001-2024-01', 'Tiếng Anh A1 - Lớp 1', '2024-02-15', '2024-05-30', 30, 'A301', 'ONGOING');

-- Insert Sample Enrollments
INSERT INTO enrollments (student_id, class_id, enrollment_date, status, payment_status, payment_amount, approved_by, approved_at) VALUES
(4, 1, '2024-02-20', 'APPROVED', 'PAID', 1500000, 1, '2024-02-21 10:00:00'),
(5, 1, '2024-02-21', 'APPROVED', 'PAID', 1500000, 1, '2024-02-22 09:30:00'),
(6, 1, '2024-02-22', 'APPROVED', 'PARTIAL', 750000, 1, '2024-02-23 11:00:00'),
(4, 2, '2024-03-01', 'APPROVED', 'PAID', 2000000, 1, '2024-03-02 14:00:00'),
(5, 3, '2024-02-10', 'APPROVED', 'PAID', 1800000, 1, '2024-02-11 16:00:00');

-- Update current_students count
UPDATE classes SET current_students = 3 WHERE id = 1;
UPDATE classes SET current_students = 1 WHERE id = 2;
UPDATE classes SET current_students = 1 WHERE id = 3;

-- Insert Sample Schedules (for class 1 - CNTT001)
INSERT INTO schedules (class_id, session_number, session_date, start_time, end_time, room, topic, status) VALUES
(1, 1, '2024-03-01', '08:00:00', '11:00:00', 'B101', 'Giới thiệu Microsoft Word', 'COMPLETED'),
(1, 2, '2024-03-04', '08:00:00', '11:00:00', 'B101', 'Định dạng văn bản nâng cao', 'COMPLETED'),
(1, 3, '2024-03-08', '08:00:00', '11:00:00', 'B101', 'Giới thiệu Microsoft Excel', 'COMPLETED'),
(1, 4, '2024-03-11', '08:00:00', '11:00:00', 'B101', 'Hàm cơ bản trong Excel', 'SCHEDULED'),
(1, 5, '2024-03-15', '08:00:00', '11:00:00', 'B101', 'Biểu đồ và đồ thị', 'SCHEDULED');

-- Insert Sample Attendances
INSERT INTO attendances (schedule_id, student_id, status, marked_by, marked_at) VALUES
-- Session 1
(1, 4, 'PRESENT', 2, '2024-03-01 08:15:00'),
(1, 5, 'PRESENT', 2, '2024-03-01 08:15:00'),
(1, 6, 'ABSENT', 2, '2024-03-01 08:15:00'),
-- Session 2
(2, 4, 'PRESENT', 2, '2024-03-04 08:10:00'),
(2, 5, 'LATE', 2, '2024-03-04 08:10:00'),
(2, 6, 'PRESENT', 2, '2024-03-04 08:10:00'),
-- Session 3
(3, 4, 'PRESENT', 2, '2024-03-08 08:05:00'),
(3, 5, 'PRESENT', 2, '2024-03-08 08:05:00'),
(3, 6, 'EXCUSED', 2, '2024-03-08 08:05:00');

-- Insert Sample Grades
INSERT INTO grades (enrollment_id, attendance_score, process_score, final_score, total_score, grade_letter, pass, graded_by, graded_at) VALUES
(1, 9.5, 8.0, 8.5, 8.7, 'A', TRUE, 2, '2024-05-20 10:00:00'),
(2, 8.0, 7.5, 7.0, 7.5, 'B', TRUE, 2, '2024-05-20 10:00:00');

-- Insert Sample Certificates
INSERT INTO certificates (enrollment_id, certificate_code, issue_date, status, issued_by) VALUES
(1, 'CERT-2024-CNTT001-001', '2024-05-25', 'ISSUED', 1),
(2, 'CERT-2024-CNTT001-002', '2024-05-25', 'ISSUED', 1);

-- =====================================================
-- VIEWS HỖ TRỢ THỐNG KÊ
-- =====================================================

-- View: Thống kê học viên theo lớp
CREATE OR REPLACE VIEW v_class_enrollment_stats AS
SELECT
    c.id AS class_id,
    c.class_code,
    c.class_name,
    co.name AS course_name,
    c.max_students,
    COUNT(e.id) AS total_enrollments,
    SUM(CASE WHEN e.status = 'APPROVED' THEN 1 ELSE 0 END) AS approved_students,
    SUM(CASE WHEN e.status = 'PENDING' THEN 1 ELSE 0 END) AS pending_students
FROM classes c
LEFT JOIN courses co ON c.course_id = co.id
LEFT JOIN enrollments e ON c.id = e.class_id
GROUP BY c.id, c.class_code, c.class_name, co.name, c.max_students;

-- View: Thống kê điểm danh theo học viên
CREATE OR REPLACE VIEW v_student_attendance_stats AS
SELECT
    e.id AS enrollment_id,
    e.student_id,
    u.full_name AS student_name,
    e.class_id,
    c.class_code,
    COUNT(a.id) AS total_sessions,
    SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) AS present_count,
    SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END) AS absent_count,
    SUM(CASE WHEN a.status = 'LATE' THEN 1 ELSE 0 END) AS late_count,
    ROUND((SUM(CASE WHEN a.status IN ('PRESENT', 'LATE') THEN 1 ELSE 0 END) / COUNT(a.id) * 100), 2) AS attendance_rate
FROM enrollments e
JOIN users u ON e.student_id = u.id
JOIN classes c ON e.class_id = c.id
LEFT JOIN schedules s ON c.id = s.class_id AND s.status = 'COMPLETED'
LEFT JOIN attendances a ON s.id = a.schedule_id AND e.student_id = a.student_id
WHERE e.status = 'APPROVED'
GROUP BY e.id, e.student_id, u.full_name, e.class_id, c.class_code;

-- View: Thống kê kết quả học tập
CREATE OR REPLACE VIEW v_grade_stats AS
SELECT
    c.id AS class_id,
    c.class_code,
    COUNT(g.id) AS total_graded,
    SUM(CASE WHEN g.pass = TRUE THEN 1 ELSE 0 END) AS passed_count,
    SUM(CASE WHEN g.pass = FALSE THEN 1 ELSE 0 END) AS failed_count,
    ROUND(AVG(g.total_score), 2) AS average_score,
    ROUND((SUM(CASE WHEN g.pass = TRUE THEN 1 ELSE 0 END) / COUNT(g.id) * 100), 2) AS pass_rate
FROM classes c
LEFT JOIN enrollments e ON c.id = e.class_id
LEFT JOIN grades g ON e.id = g.enrollment_id
WHERE e.status = 'APPROVED'
GROUP BY c.id, c.class_code;

-- =====================================================
-- END OF SCRIPT
-- =====================================================

-- NOTE: Tài khoản đăng nhập demo:
-- Username: admin, Password: 123456 (ADMIN)
-- Username: teacher1, Password: 123456 (TEACHER)
-- Username: student1, Password: 123456 (STUDENT)
