-- =====================================================
-- SAMPLE DATA SCRIPT FOR TESTING GRADING FUNCTIONALITY
-- =====================================================
-- Purpose: Add comprehensive sample data to test class grading features
-- Date: 2024
-- Note: This script assumes the database schema from DATABASE_MVP.sql exists
-- =====================================================

USE short_term_training;

-- =====================================================
-- PART 1: DELETE EXISTING TEST DATA (Optional)
-- =====================================================
-- Uncomment these lines if you want to reset test data
/*
DELETE FROM certificates WHERE issued_by IN (
    SELECT id FROM users WHERE username IN ('teacher_grade_test')
);
DELETE FROM grades WHERE graded_by IN (
    SELECT id FROM users WHERE username IN ('teacher_grade_test')
);
DELETE FROM attendances WHERE marked_by IN (
    SELECT id FROM users WHERE username IN ('teacher_grade_test')
);
DELETE FROM schedules WHERE class_id IN (
    SELECT id FROM classes WHERE teacher_id IN (
        SELECT id FROM users WHERE username = 'teacher_grade_test'
    )
);
DELETE FROM enrollments WHERE student_id IN (
    SELECT id FROM users WHERE username LIKE 'student_grade_test%'
);
DELETE FROM classes WHERE teacher_id IN (
    SELECT id FROM users WHERE username = 'teacher_grade_test'
);
*/

-- =====================================================
-- PART 2: CREATE TEST USERS
-- =====================================================
-- Teacher for grading test
INSERT IGNORE INTO users (username, email, password, full_name, phone, role, status)
VALUES (
    'teacher_grade_test', 
    'teacher.gradetest@nute.edu.vn',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',  -- password: 123456
    'Phạm Thị Chấm Điểm',
    '0912345690',
    'TEACHER',
    'ACTIVE'
);

-- Students for grading test (10 students)
INSERT IGNORE INTO users (username, email, password, full_name, phone, role, status) VALUES
('student_grade_01', 'student.grade01@nute.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Trần Quốc Anh', '0912345701', 'STUDENT', 'ACTIVE'),
('student_grade_02', 'student.grade02@nute.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Nguyễn Thị Bảo', '0912345702', 'STUDENT', 'ACTIVE'),
('student_grade_03', 'student.grade03@nute.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Lê Văn Cường', '0912345703', 'STUDENT', 'ACTIVE'),
('student_grade_04', 'student.grade04@nute.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Phạm Thị Duyên', '0912345704', 'STUDENT', 'ACTIVE'),
('student_grade_05', 'student.grade05@nute.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Hoàng Văn Em', '0912345705', 'STUDENT', 'ACTIVE'),
('student_grade_06', 'student.grade06@nute.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Vũ Thị Phương', '0912345706', 'STUDENT', 'ACTIVE'),
('student_grade_07', 'student.grade07@nute.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Đỗ Văn Giang', '0912345707', 'STUDENT', 'ACTIVE'),
('student_grade_08', 'student.grade08@nute.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Bùi Thị Hương', '0912345708', 'STUDENT', 'ACTIVE'),
('student_grade_09', 'student.grade09@nute.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Cao Văn Ích', '0912345709', 'STUDENT', 'ACTIVE'),
('student_grade_10', 'student.grade10@nute.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Nông Thị Kiều', '0912345710', 'STUDENT', 'ACTIVE');

-- =====================================================
-- PART 3: CREATE COURSE TYPE AND COURSE FOR TESTING
-- =====================================================
INSERT IGNORE INTO course_types (code, name, description)
VALUES ('CNTT-TEST', 'Công Nghệ Thông Tin - Test', 'Khóa học test chức năng chấm điểm');

-- Get IDs from inserted data
SET @courseTypeId = (SELECT id FROM course_types WHERE code = 'CNTT-TEST' LIMIT 1);
SET @teacherId = (SELECT id FROM users WHERE username = 'teacher_grade_test' LIMIT 1);
SET @adminId = (SELECT id FROM users WHERE role = 'ADMIN' LIMIT 1);

-- Insert test course
INSERT IGNORE INTO courses (
    course_type_id, code, name, description, 
    duration_hours, duration_sessions, tuition_fee, 
    max_students, status, created_by
) VALUES (
    @courseTypeId,
    'CNTT-GRADE-TEST-001',
    'Lập Trình Java Cơ Bản - Class Test Chấm Điểm',
    'Khóa học Java cơ bản để test chức năng chấm điểm lớp học',
    60,
    15,
    2000000,
    30,
    'ACTIVE',
    @adminId
);

-- =====================================================
-- PART 4: CREATE TEST CLASS
-- =====================================================
SET @courseId = (SELECT id FROM courses WHERE code = 'CNTT-GRADE-TEST-001' LIMIT 1);

INSERT IGNORE INTO classes (
    course_id, teacher_id, class_code, class_name,
    start_date, end_date, max_students, current_students,
    room, status
) VALUES (
    @courseId,
    @teacherId,
    'JAVA-GRADE-TEST-K01',
    'Lập Trình Java Cơ Bản - Lớp Test Chấm Điểm K01',
    DATE_SUB(CURDATE(), INTERVAL 30 DAY),
    DATE_ADD(CURDATE(), INTERVAL 15 DAY),
    30,
    10,
    'Phòng B201',
    'ONGOING'
);

-- =====================================================
-- PART 5: ENROLL STUDENTS IN TEST CLASS
-- =====================================================
SET @classId = (SELECT id FROM classes WHERE class_code = 'JAVA-GRADE-TEST-K01' LIMIT 1);

-- Get student IDs
SET @student01 = (SELECT id FROM users WHERE username = 'student_grade_01' LIMIT 1);
SET @student02 = (SELECT id FROM users WHERE username = 'student_grade_02' LIMIT 1);
SET @student03 = (SELECT id FROM users WHERE username = 'student_grade_03' LIMIT 1);
SET @student04 = (SELECT id FROM users WHERE username = 'student_grade_04' LIMIT 1);
SET @student05 = (SELECT id FROM users WHERE username = 'student_grade_05' LIMIT 1);
SET @student06 = (SELECT id FROM users WHERE username = 'student_grade_06' LIMIT 1);
SET @student07 = (SELECT id FROM users WHERE username = 'student_grade_07' LIMIT 1);
SET @student08 = (SELECT id FROM users WHERE username = 'student_grade_08' LIMIT 1);
SET @student09 = (SELECT id FROM users WHERE username = 'student_grade_09' LIMIT 1);
SET @student10 = (SELECT id FROM users WHERE username = 'student_grade_10' LIMIT 1);

INSERT IGNORE INTO enrollments (
    student_id, class_id, enrollment_date, status,
    payment_status, payment_amount, approved_by, approved_at
) VALUES
(@student01, @classId, DATE_SUB(CURDATE(), INTERVAL 35 DAY), 'APPROVED', 'PAID', 2000000, @adminId, NOW()),
(@student02, @classId, DATE_SUB(CURDATE(), INTERVAL 34 DAY), 'APPROVED', 'PAID', 2000000, @adminId, NOW()),
(@student03, @classId, DATE_SUB(CURDATE(), INTERVAL 33 DAY), 'APPROVED', 'PAID', 2000000, @adminId, NOW()),
(@student04, @classId, DATE_SUB(CURDATE(), INTERVAL 32 DAY), 'APPROVED', 'PAID', 2000000, @adminId, NOW()),
(@student05, @classId, DATE_SUB(CURDATE(), INTERVAL 31 DAY), 'APPROVED', 'PARTIAL', 1000000, @adminId, NOW()),
(@student06, @classId, DATE_SUB(CURDATE(), INTERVAL 30 DAY), 'APPROVED', 'PAID', 2000000, @adminId, NOW()),
(@student07, @classId, DATE_SUB(CURDATE(), INTERVAL 29 DAY), 'APPROVED', 'UNPAID', 0, @adminId, NOW()),
(@student08, @classId, DATE_SUB(CURDATE(), INTERVAL 28 DAY), 'APPROVED', 'PAID', 2000000, @adminId, NOW()),
(@student09, @classId, DATE_SUB(CURDATE(), INTERVAL 27 DAY), 'APPROVED', 'PAID', 2000000, @adminId, NOW()),
(@student10, @classId, DATE_SUB(CURDATE(), INTERVAL 26 DAY), 'APPROVED', 'PAID', 2000000, @adminId, NOW());

-- =====================================================
-- PART 6: CREATE SCHEDULES FOR TEST CLASS
-- =====================================================
-- Create 15 sessions (as per course duration_sessions)

INSERT IGNORE INTO schedules (
    class_id, session_number, session_date, 
    start_time, end_time, room, topic, description, status
) VALUES
-- Week 1
(@classId, 1, DATE_SUB(CURDATE(), INTERVAL 28 DAY), '08:00:00', '10:00:00', 'Phòng B201', 'Java Introduction & Environment Setup', 'Cài đặt môi trường và giới thiệu Java cơ bản', 'COMPLETED'),
(@classId, 2, DATE_SUB(CURDATE(), INTERVAL 26 DAY), '08:00:00', '10:00:00', 'Phòng B201', 'Biến, Kiểu dữ liệu, Operator', 'Học về các kiểu dữ liệu và toán tử trong Java', 'COMPLETED'),
(@classId, 3, DATE_SUB(CURDATE(), INTERVAL 24 DAY), '08:00:00', '10:00:00', 'Phòng B201', 'Control Flow: If/Else, Switch', 'Cấu trúc điều khiển dòng chảy chương trình', 'COMPLETED'),
(@classId, 4, DATE_SUB(CURDATE(), INTERVAL 22 DAY), '08:00:00', '10:00:00', 'Phòng B201', 'Loops: For, While, Do-While', 'Các vòng lặp và cách sử dụng', 'COMPLETED'),
(@classId, 5, DATE_SUB(CURDATE(), INTERVAL 20 DAY), '08:00:00', '10:00:00', 'Phòng B201', 'Arrays và Collections', 'Mảng và các collection trong Java', 'COMPLETED'),

-- Week 2
(@classId, 6, DATE_SUB(CURDATE(), INTERVAL 18 DAY), '08:00:00', '10:00:00', 'Phòng B201', 'Functions & Methods', 'Khai báo và sử dụng hàm/phương thức', 'COMPLETED'),
(@classId, 7, DATE_SUB(CURDATE(), INTERVAL 16 DAY), '08:00:00', '10:00:00', 'Phòng B201', 'OOP: Classes & Objects', 'Lập trình hướng đối tượng - Lớp và đối tượng', 'COMPLETED'),
(@classId, 8, DATE_SUB(CURDATE(), INTERVAL 14 DAY), '08:00:00', '10:00:00', 'Phòng B201', 'OOP: Inheritance & Polymorphism', 'Kế thừa và đa hình trong OOP', 'COMPLETED'),
(@classId, 9, DATE_SUB(CURDATE(), INTERVAL 12 DAY), '08:00:00', '10:00:00', 'Phòng B201', 'Exception Handling', 'Xử lý ngoại lệ trong Java', 'COMPLETED'),
(@classId, 10, DATE_SUB(CURDATE(), INTERVAL 10 DAY), '08:00:00', '10:00:00', 'Phòng B201', 'File I/O & Streams', 'Làm việc với file và stream', 'COMPLETED'),

-- Week 3
(@classId, 11, DATE_SUB(CURDATE(), INTERVAL 8 DAY), '08:00:00', '10:00:00', 'Phòng B201', 'Database Connection & JDBC', 'Kết nối cơ sở dữ liệu với JDBC', 'COMPLETED'),
(@classId, 12, DATE_SUB(CURDATE(), INTERVAL 6 DAY), '08:00:00', '10:00:00', 'Phòng B201', 'Project Discussion & Demo', 'Thảo luận dự án và demo ứng dụng', 'COMPLETED'),
(@classId, 13, DATE_SUB(CURDATE(), INTERVAL 4 DAY), '08:00:00', '10:00:00', 'Phòng B201', 'Bài tập lập trình - Phần 1', 'Thực hành lập trình phần 1', 'COMPLETED'),
(@classId, 14, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '08:00:00', '10:00:00', 'Phòng B201', 'Bài tập lập trình - Phần 2', 'Thực hành lập trình phần 2', 'COMPLETED'),
(@classId, 15, CURDATE(), '08:00:00', '10:00:00', 'Phòng B201', 'Ôn tập & Thi cuối khóa', 'Ôn tập kiến thức và thi cuối kỳ', 'SCHEDULED');

-- =====================================================
-- PART 7: CREATE ATTENDANCE RECORDS
-- =====================================================
-- Note: Fill attendance for first 14 sessions (completed sessions)
-- Simulate different attendance scenarios

INSERT IGNORE INTO attendances (schedule_id, student_id, status, note, marked_by, marked_at) VALUES
-- Session 1 (All present)
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 1), @student01, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 1), @student02, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 1), @student03, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 1), @student04, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 1), @student05, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 1), @student06, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 1), @student07, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 1), @student08, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 1), @student09, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 1), @student10, 'PRESENT', '', @teacherId, NOW()),

-- Session 2 (Some late, one absent)
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 2), @student01, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 2), @student02, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 2), @student03, 'LATE', 'Muộn 15 phút', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 2), @student04, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 2), @student05, 'ABSENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 2), @student06, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 2), @student07, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 2), @student08, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 2), @student09, 'LATE', 'Muộn 10 phút', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 2), @student10, 'PRESENT', '', @teacherId, NOW()),

-- Session 3-5: Mix of present/late/absent/excused for variance
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 3), @student01, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 3), @student02, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 3), @student03, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 3), @student04, 'ABSENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 3), @student05, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 3), @student06, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 3), @student07, 'EXCUSED', 'Bệnh', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 3), @student08, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 3), @student09, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 3), @student10, 'PRESENT', '', @teacherId, NOW()),

((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 4), @student01, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 4), @student02, 'LATE', 'Muộn 20 phút', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 4), @student03, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 4), @student04, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 4), @student05, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 4), @student06, 'ABSENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 4), @student07, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 4), @student08, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 4), @student09, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 4), @student10, 'EXCUSED', 'Việc gia đình', @teacherId, NOW()),

((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 5), @student01, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 5), @student02, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 5), @student03, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 5), @student04, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 5), @student05, 'ABSENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 5), @student06, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 5), @student07, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 5), @student08, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 5), @student09, 'LATE', 'Muộn 5 phút', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 5), @student10, 'PRESENT', '', @teacherId, NOW()),

-- Sessions 6-14: Generate random but consistent attendance (all present for simplicity)
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 6), @student01, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 6), @student02, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 6), @student03, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 6), @student04, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 6), @student05, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 6), @student06, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 6), @student07, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 6), @student08, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 6), @student09, 'PRESENT', '', @teacherId, NOW()),
((SELECT id FROM schedules WHERE class_id = @classId AND session_number = 6), @student10, 'PRESENT', '', @teacherId, NOW());

-- Fill remaining sessions with all present (sessions 7-14)
INSERT IGNORE INTO attendances (schedule_id, student_id, status, marked_by, marked_at)
SELECT s.id, @student01, 'PRESENT', @teacherId, NOW()
FROM schedules s 
WHERE s.class_id = @classId AND s.session_number IN (7,8,9,10,11,12,13,14)
UNION ALL
SELECT s.id, @student02, 'PRESENT', @teacherId, NOW()
FROM schedules s 
WHERE s.class_id = @classId AND s.session_number IN (7,8,9,10,11,12,13,14)
UNION ALL
SELECT s.id, @student03, 'PRESENT', @teacherId, NOW()
FROM schedules s 
WHERE s.class_id = @classId AND s.session_number IN (7,8,9,10,11,12,13,14)
UNION ALL
SELECT s.id, @student04, 'PRESENT', @teacherId, NOW()
FROM schedules s 
WHERE s.class_id = @classId AND s.session_number IN (7,8,9,10,11,12,13,14)
UNION ALL
SELECT s.id, @student05, 'PRESENT', @teacherId, NOW()
FROM schedules s 
WHERE s.class_id = @classId AND s.session_number IN (7,8,9,10,11,12,13,14)
UNION ALL
SELECT s.id, @student06, 'PRESENT', @teacherId, NOW()
FROM schedules s 
WHERE s.class_id = @classId AND s.session_number IN (7,8,9,10,11,12,13,14)
UNION ALL
SELECT s.id, @student07, 'PRESENT', @teacherId, NOW()
FROM schedules s 
WHERE s.class_id = @classId AND s.session_number IN (7,8,9,10,11,12,13,14)
UNION ALL
SELECT s.id, @student08, 'PRESENT', @teacherId, NOW()
FROM schedules s 
WHERE s.class_id = @classId AND s.session_number IN (7,8,9,10,11,12,13,14)
UNION ALL
SELECT s.id, @student09, 'PRESENT', @teacherId, NOW()
FROM schedules s 
WHERE s.class_id = @classId AND s.session_number IN (7,8,9,10,11,12,13,14)
UNION ALL
SELECT s.id, @student10, 'PRESENT', @teacherId, NOW()
FROM schedules s 
WHERE s.class_id = @classId AND s.session_number IN (7,8,9,10,11,12,13,14);

-- =====================================================
-- PART 8: INSERT GRADE RECORDS WITH DIFFERENT SCORES
-- =====================================================
-- We'll create different grade scenarios:
-- Student 01: High performer (8.0+)
-- Student 02: Good performer (7.0-7.9)
-- Student 03: Average performer (6.0-6.9)
-- Student 04: Below average (5.0-5.9)
-- Student 05: Low performer (4.0-4.9)
-- Student 06: Excellent (9.0+)
-- Student 07: Fail (< 5.0)
-- Students 08-10: Various scores

INSERT IGNORE INTO grades (
    enrollment_id, attendance_score, process_score, 
    final_score, total_score, grade_letter, pass, 
    graded_by, graded_at
) VALUES
-- Student 01: High performer
(
    (SELECT e.id FROM enrollments e 
     JOIN users u ON e.student_id = u.id 
     WHERE e.class_id = @classId AND u.username = 'student_grade_01' LIMIT 1),
    9.0, 8.5, 8.2, 8.6, 'A', TRUE, @teacherId, NOW()
),

-- Student 02: Good performer
(
    (SELECT e.id FROM enrollments e 
     JOIN users u ON e.student_id = u.id 
     WHERE e.class_id = @classId AND u.username = 'student_grade_02' LIMIT 1),
    8.5, 7.5, 7.0, 7.7, 'B', TRUE, @teacherId, NOW()
),

-- Student 03: Average performer
(
    (SELECT e.id FROM enrollments e 
     JOIN users u ON e.student_id = u.id 
     WHERE e.class_id = @classId AND u.username = 'student_grade_03' LIMIT 1),
    7.0, 6.5, 6.2, 6.6, 'C', TRUE, @teacherId, NOW()
),

-- Student 04: Below average
(
    (SELECT e.id FROM enrollments e 
     JOIN users u ON e.student_id = u.id 
     WHERE e.class_id = @classId AND u.username = 'student_grade_04' LIMIT 1),
    6.5, 5.5, 5.0, 5.7, 'D', TRUE, @teacherId, NOW()
),

-- Student 05: Low performer (but still pass)
(
    (SELECT e.id FROM enrollments e 
     JOIN users u ON e.student_id = u.id 
     WHERE e.class_id = @classId AND u.username = 'student_grade_05' LIMIT 1),
    5.5, 4.8, 4.5, 4.9, 'F', FALSE, @teacherId, NOW()
),

-- Student 06: Excellent
(
    (SELECT e.id FROM enrollments e 
     JOIN users u ON e.student_id = u.id 
     WHERE e.class_id = @classId AND u.username = 'student_grade_06' LIMIT 1),
    10.0, 9.5, 9.0, 9.5, 'A', TRUE, @teacherId, NOW()
),

-- Student 07: Failed course
(
    (SELECT e.id FROM enrollments e 
     JOIN users u ON e.student_id = u.id 
     WHERE e.class_id = @classId AND u.username = 'student_grade_07' LIMIT 1),
    4.5, 4.0, 3.5, 4.0, 'F', FALSE, @teacherId, NOW()
),

-- Student 08: Good performer
(
    (SELECT e.id FROM enrollments e 
     JOIN users u ON e.student_id = u.id 
     WHERE e.class_id = @classId AND u.username = 'student_grade_08' LIMIT 1),
    8.5, 8.0, 8.5, 8.3, 'A', TRUE, @teacherId, NOW()
),

-- Student 09: Average performer
(
    (SELECT e.id FROM enrollments e 
     JOIN users u ON e.student_id = u.id 
     WHERE e.class_id = @classId AND u.username = 'student_grade_09' LIMIT 1),
    7.5, 6.8, 6.5, 6.9, 'B', TRUE, @teacherId, NOW()
),

-- Student 10: Good performer
(
    (SELECT e.id FROM enrollments e 
     JOIN users u ON e.student_id = u.id 
     WHERE e.class_id = @classId AND u.username = 'student_grade_10' LIMIT 1),
    8.0, 7.5, 7.8, 7.8, 'B', TRUE, @teacherId, NOW()
);

-- =====================================================
-- PART 9: INSERT CERTIFICATE RECORDS FOR PASSED STUDENTS
-- =====================================================
INSERT IGNORE INTO certificates (
    enrollment_id, certificate_code, issue_date, 
    status, issued_by
) 
SELECT 
    e.id,
    CONCAT('CERT-GRADE-TEST-', LPAD(ROW_NUMBER() OVER (ORDER BY e.id), 3, '0')),
    CURDATE(),
    'ISSUED',
    @adminId
FROM enrollments e
JOIN grades g ON e.id = g.enrollment_id
WHERE e.class_id = @classId AND g.pass = TRUE;

-- =====================================================
-- PART 10: VERIFICATION QUERIES
-- =====================================================
-- Run these queries to verify the data has been inserted correctly

SELECT '=== SUMMARY OF INSERTED TEST DATA ===' AS Info;

SELECT CONCAT('Total Students: ', COUNT(*)) AS Statistic
FROM enrollments WHERE class_id = @classId;

SELECT CONCAT('Completed Sessions: ', COUNT(*)) AS Statistic
FROM schedules WHERE class_id = @classId AND status = 'COMPLETED';

SELECT CONCAT('Total Attendance Records: ', COUNT(*)) AS Statistic
FROM attendances a
JOIN schedules s ON a.schedule_id = s.id
WHERE s.class_id = @classId;

SELECT CONCAT('Graded Students: ', COUNT(*)) AS Statistic
FROM grades g
JOIN enrollments e ON g.enrollment_id = e.id
WHERE e.class_id = @classId;

SELECT CONCAT('Passed Students: ', COUNT(*)) AS Statistic
FROM grades g
JOIN enrollments e ON g.enrollment_id = e.id
WHERE e.class_id = @classId AND g.pass = TRUE;

SELECT CONCAT('Failed Students: ', COUNT(*)) AS Statistic
FROM grades g
JOIN enrollments e ON g.enrollment_id = e.id
WHERE e.class_id = @classId AND g.pass = FALSE;

SELECT CONCAT('Certificates Issued: ', COUNT(*)) AS Statistic
FROM certificates c
JOIN enrollments e ON c.enrollment_id = e.id
WHERE e.class_id = @classId;

-- =====================================================
-- PART 11: USEFUL QUERIES FOR TESTING
-- =====================================================

-- View detailed grades by student
SELECT 
    u.full_name AS 'Họ và Tên',
    c.class_code AS 'Mã Lớp',
    g.attendance_score AS 'Điểm Chuyên Cần',
    g.process_score AS 'Điểm Quá Trình',
    g.final_score AS 'Điểm Thi',
    g.total_score AS 'Điểm Tổng',
    g.grade_letter AS 'Xếp Loại',
    IF(g.pass, 'Đạt', 'Không Đạt') AS 'Kết Quả',
    g.graded_at AS 'Thời Gian Chấm'
FROM grades g
JOIN enrollments e ON g.enrollment_id = e.id
JOIN users u ON e.student_id = u.id
JOIN classes c ON e.class_id = c.id
WHERE c.class_code = 'JAVA-GRADE-TEST-K01'
ORDER BY g.total_score DESC;

-- View attendance summary by student
SELECT 
    u.full_name AS 'Họ và Tên',
    COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END) AS 'Có Mặt',
    COUNT(CASE WHEN a.status = 'ABSENT' THEN 1 END) AS 'Vắng',
    COUNT(CASE WHEN a.status = 'LATE' THEN 1 END) AS 'Muộn',
    COUNT(CASE WHEN a.status = 'EXCUSED' THEN 1 END) AS 'Có Phép',
    ROUND((COUNT(CASE WHEN a.status IN ('PRESENT', 'LATE') THEN 1 END) / 
            COUNT(*) * 100), 1) AS 'Tỷ Lệ (%)'
FROM attendances a
JOIN users u ON a.student_id = u.id
JOIN schedules s ON a.schedule_id = s.id
WHERE s.class_id = @classId
GROUP BY u.id, u.full_name
ORDER BY u.full_name;

-- View class statistics
SELECT 
    'JAVA-GRADE-TEST-K01' AS 'Lớp Học',
    COUNT(DISTINCT e.student_id) AS 'Tổng SV Đăng Ký',
    COUNT(DISTINCT g.id) AS 'Số SV Đã Chấm',
    COUNT(DISTINCT CASE WHEN g.pass = TRUE THEN g.id END) AS 'Số SV Đạt',
    COUNT(DISTINCT CASE WHEN g.pass = FALSE THEN g.id END) AS 'Số SV Không Đạt',
    ROUND(AVG(g.total_score), 2) AS 'Điểm TB Lớp'
FROM enrollments e
LEFT JOIN grades g ON e.enrollment_id = g.id
WHERE e.class_id = @classId;

-- =====================================================
-- END OF SAMPLE DATA SCRIPT
-- =====================================================
-- Notes:
-- - Demo teacher login: teacher_grade_test / 123456
-- - Demo student logins: student_grade_01 to student_grade_10 / 123456
-- - All passwords are: 123456 (BCrypt hash in password field)
-- - Test class code: JAVA-GRADE-TEST-K01
-- - Different grade scenarios for testing various functionality
-- =====================================================
