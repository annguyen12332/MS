-- Dummy Data Script for Short Term Training Management System
-- Based on constraints: Teacher ID = 4, Student IDs = 1, 2
-- Minimum 5 records per table

USE short_term_training;

-- ========================================================
-- 1. Course Types (5 records)
-- ========================================================
INSERT INTO course_types (code, name, description, created_at) VALUES 
('CNTT', 'Công nghệ thông tin', 'Các khóa học về lập trình, phần mềm, mạng máy tính', NOW()),
('NN', 'Ngoại ngữ', 'Tiếng Anh, Tiếng Nhật, Tiếng Hàn, Tiếng Trung', NOW()),
('KNM', 'Kỹ năng mềm', 'Kỹ năng giao tiếp, thuyết trình, làm việc nhóm, quản lý thời gian', NOW()),
('TKDH', 'Thiết kế đồ họa', 'Photoshop, Illustrator, UI/UX Design, Dựng phim', NOW()),
('QTKD', 'Quản trị kinh doanh', 'Marketing, Bán hàng, Khởi nghiệp, Quản trị nhân sự', NOW());

-- ========================================================
-- 2. Courses (5 records)
-- created_by = 4 (Teacher)
-- ========================================================
-- Assuming IDs 1-5 for Course Types generated above
INSERT INTO courses (code, name, description, duration_hours, duration_sessions, tuition_fee, max_students, requirements, status, course_type_id, created_by, created_at, updated_at) VALUES
('JAVA01', 'Lập trình Java Căn Bản', 'Khóa học Java nền tảng cho người mới bắt đầu', 40, 20, 2000000, 30, 'Có máy tính cá nhân', 'ACTIVE', 1, 4, NOW(), NOW()),
('ENG01', 'Tiếng Anh Giao Tiếp Sơ Cấp', 'Luyện kỹ năng nghe nói phản xạ cơ bản', 30, 15, 1500000, 20, 'Không yêu cầu đầu vào', 'ACTIVE', 2, 4, NOW(), NOW()),
('SOFT01', 'Kỹ năng Thuyết Trình', 'Tự tin nói trước đám đông và trình bày ý tưởng', 10, 5, 500000, 25, 'Không yêu cầu', 'ACTIVE', 3, 4, NOW(), NOW()),
('DES01', 'Photoshop Cơ Bản', 'Chỉnh sửa ảnh chuyên nghiệp với Adobe Photoshop', 24, 12, 1800000, 20, 'Máy tính cài sẵn Photoshop', 'ACTIVE', 4, 4, NOW(), NOW()),
('MKT01', 'Digital Marketing 101', 'Tổng quan về Marketing trên nền tảng số', 20, 10, 1200000, 40, 'Thích kinh doanh', 'ACTIVE', 5, 4, NOW(), NOW());

-- ========================================================
-- 3. Classes (5 records)
-- teacher_id = 4
-- ========================================================
-- Assuming IDs 1-5 for Courses generated above
INSERT INTO classes (class_code, class_name, start_date, end_date, max_students, current_students, room, status, course_id, teacher_id, created_at, updated_at) VALUES
('JAVA-K14-01', 'Lớp Java Cơ Bản K14', DATE_ADD(CURDATE(), INTERVAL -30 DAY), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 30, 2, 'P.301 - Khu A', 'ONGOING', 1, 4, NOW(), NOW()),
('ENG-K14-01', 'Lớp Tiếng Anh GT K14', DATE_ADD(CURDATE(), INTERVAL -20 DAY), DATE_ADD(CURDATE(), INTERVAL 10 DAY), 20, 1, 'P.202 - Khu B', 'ONGOING', 2, 4, NOW(), NOW()),
('SOFT-K15-01', 'Lớp Kỹ Năng K15', DATE_ADD(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 20 DAY), 25, 0, 'Hội trường C', 'PENDING', 3, 4, NOW(), NOW()),
('DES-K14-01', 'Lớp Photoshop K14', DATE_ADD(CURDATE(), INTERVAL -60 DAY), DATE_ADD(CURDATE(), INTERVAL -10 DAY), 20, 2, 'P.Mac - Khu A', 'COMPLETED', 4, 4, NOW(), NOW()),
('MKT-K15-01', 'Lớp Marketing K15', DATE_ADD(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 25 DAY), 40, 1, 'Online Zoom', 'PENDING', 5, 4, NOW(), NOW());

-- ========================================================
-- 4. Enrollments (6 records)
-- Students: 1, 2
-- ========================================================
-- Class 1 (Java - Ongoing): Student 1, 2
-- Class 2 (Eng - Ongoing): Student 2
-- Class 3 (Soft - Pending): Student 1
-- Class 4 (Des - Completed): Student 1, 2
-- Class 5 (Mkt - Pending): No enrollments yet? Let's add Student 1.
INSERT INTO enrollments (enrollment_date, status, payment_status, payment_amount, notes, student_id, class_id, approved_by, approved_at, created_at, updated_at) VALUES
(DATE_ADD(CURDATE(), INTERVAL -35 DAY), 'APPROVED', 'PAID', 2000000, 'Đóng đủ', 1, 1, 4, NOW(), NOW(), NOW()),
(DATE_ADD(CURDATE(), INTERVAL -34 DAY), 'APPROVED', 'PAID', 2000000, 'Chuyển khoản', 2, 1, 4, NOW(), NOW(), NOW()),
(DATE_ADD(CURDATE(), INTERVAL -25 DAY), 'APPROVED', 'PARTIAL', 500000, 'Đóng cọc', 2, 2, 4, NOW(), NOW(), NOW()),
(DATE_ADD(CURDATE(), INTERVAL -5 DAY), 'PENDING', 'UNPAID', 0, 'Chờ xác nhận', 1, 3, NULL, NULL, NOW(), NOW()),
(DATE_ADD(CURDATE(), INTERVAL -65 DAY), 'COMPLETED', 'PAID', 1800000, 'Hoàn thành khóa học', 1, 4, 4, NOW(), NOW(), NOW()),
(DATE_ADD(CURDATE(), INTERVAL -64 DAY), 'COMPLETED', 'PAID', 1800000, 'Xuất sắc', 2, 4, 4, NOW(), NOW(), NOW());

-- ========================================================
-- 5. Schedules (6 records)
-- ========================================================
-- Class 1 (Java): 2 sessions past
-- Class 2 (Eng): 1 session past
-- Class 4 (Des - Completed): 2 sessions past
-- Class 5 (Mkt): 1 session future
INSERT INTO schedules (session_number, session_date, start_time, end_time, room, topic, description, status, class_id, created_at, updated_at) VALUES
(1, DATE_ADD(CURDATE(), INTERVAL -28 DAY), '18:00:00', '20:00:00', 'P.301 - Khu A', 'Java Introduction', 'Giới thiệu về Java và cài đặt môi trường', 'COMPLETED', 1, NOW(), NOW()),
(2, DATE_ADD(CURDATE(), INTERVAL -25 DAY), '18:00:00', '20:00:00', 'P.301 - Khu A', 'Variables and DataTypes', 'Biến và kiểu dữ liệu trong Java', 'COMPLETED', 1, NOW(), NOW()),
(1, DATE_ADD(CURDATE(), INTERVAL -18 DAY), '17:30:00', '19:30:00', 'P.202 - Khu B', 'Greeting & Introduction', 'Chào hỏi và giới thiệu bản thân', 'COMPLETED', 2, NOW(), NOW()),
(1, DATE_ADD(CURDATE(), INTERVAL -58 DAY), '18:00:00', '20:00:00', 'P.Mac - Khu A', 'Photoshop Interface', 'Giao diện làm việc của Photoshop', 'COMPLETED', 4, NOW(), NOW()),
(2, DATE_ADD(CURDATE(), INTERVAL -55 DAY), '18:00:00', '20:00:00', 'P.Mac - Khu A', 'Layers and Selections', 'Làm việc với Layer và vùng chọn', 'COMPLETED', 4, NOW(), NOW()),
(1, DATE_ADD(CURDATE(), INTERVAL 6 DAY), '19:00:00', '21:00:00', 'Online Zoom', 'Digital Marketing Overview', 'Tổng quan về Digital Marketing', 'SCHEDULED', 5, NOW(), NOW());

-- ========================================================
-- 6. Attendances (7 records)
-- Students 1, 2 in above schedules
-- ========================================================
-- Schedule 1 (Class 1): Student 1 (Present), Student 2 (Present)
-- Schedule 2 (Class 1): Student 1 (Present), Student 2 (Late)
-- Schedule 3 (Class 2): Student 2 (Present)
-- Schedule 4 (Class 4): Student 1 (Present), Student 2 (Present)
-- Schedule 5 (Class 4): Student 1 (Present), Student 2 (Excused)
INSERT INTO attendances (status, note, student_id, schedule_id, marked_by, marked_at, created_at, updated_at) VALUES
('PRESENT', 'Đúng giờ', 1, 1, 4, NOW(), NOW(), NOW()),
('PRESENT', 'Đúng giờ', 2, 1, 4, NOW(), NOW(), NOW()),
('PRESENT', 'Tốt', 1, 2, 4, NOW(), NOW(), NOW()),
('LATE', 'Đến muộn 15p', 2, 2, 4, NOW(), NOW(), NOW()),
('PRESENT', '', 2, 3, 4, NOW(), NOW(), NOW()),
('PRESENT', '', 1, 4, 4, NOW(), NOW(), NOW()),
('EXCUSED', 'Bị ốm', 2, 5, 4, NOW(), NOW(), NOW());

-- ========================================================
-- 7. Grades (6 records)
-- Linked to Enrollments
-- ========================================================
-- Enrollment 1 (Std 1, Class 1): In progress
-- Enrollment 2 (Std 2, Class 1): In progress
-- Enrollment 3 (Std 2, Class 2): In progress
-- Enrollment 4 (Std 1, Class 3): Pending
-- Enrollment 5 (Std 1, Class 4): Completed (High score)
-- Enrollment 6 (Std 2, Class 4): Completed (Medium score)

-- Note: ID of enrollments generated sequentially 1..6
INSERT INTO grades (attendance_score, process_score, final_score, total_score, grade_letter, pass, note, enrollment_id, graded_by, graded_at, created_at, updated_at) VALUES
(9.0, 8.0, NULL, NULL, NULL, 0, 'Chưa thi cuối kỳ', 1, 4, NOW(), NOW(), NOW()),
(8.0, 7.5, NULL, NULL, NULL, 0, 'Chưa thi cuối kỳ', 2, 4, NOW(), NOW(), NOW()),
(9.5, 8.5, NULL, NULL, NULL, 0, 'Học tốt', 3, 4, NOW(), NOW(), NOW()),
(NULL, NULL, NULL, NULL, NULL, 0, 'Chưa bắt đầu', 4, NULL, NULL, NOW(), NOW()),
(10.0, 9.0, 9.5, 9.4, 'A', 1, 'Xuất sắc', 5, 4, NOW(), NOW(), NOW()),
(8.0, 7.0, 6.5, 6.8, 'C', 1, 'Đạt', 6, 4, NOW(), NOW(), NOW());

-- ========================================================
-- 8. Certificates (5 records)
-- Linked to Enrollments that are COMPLETED
-- ========================================================
-- Since only Enrollments 5 and 6 are truly 'COMPLETED' with scores, 
-- I will fake some others as "ISSUED" just to meet the "5 records" requirement,
-- or I can insert more completed enrollments/classes if strictly needed,
-- but sticking to the request, I will generate certificates for the completed ones
-- and maybe some historical/migrated ones if logic permits. 
-- Let's just create 5 certificates, assuming some previous data or just fulfilling the volume requirement.
-- We will use Enrollments 5 and 6. 
-- For the other 3, I will create dummy historical enrollments or just attach to current ones as "Draft" or "Issued" for testing.
-- Actually, let's add a few more "COMPLETED" enrollments for historical classes to make it clean.

-- Adding a few more enrollments to Class 4 (Completed) to hang certificates on, or just attach to existing.
-- To be safe, I will just add certificates for enrollment 5 and 6, and then 3 more dummy certificates for hypothetical old enrollments
-- OR I can just add certificates for the existing enrollments even if they are 'in progress' just for data volume (marked as DRAFT).

INSERT INTO certificates (certificate_code, issue_date, file_path, status, notes, enrollment_id, issued_by, created_at, updated_at) VALUES
('CERT-2023-JAVA-001', CURDATE(), '/certs/java_001.pdf', 'ISSUED', 'Chứng chỉ xuất sắc', 5, 4, NOW(), NOW()),
('CERT-2023-JAVA-002', CURDATE(), '/certs/java_002.pdf', 'ISSUED', 'Chứng chỉ hoàn thành', 6, 4, NOW(), NOW()),
('CERT-DRAFT-001', CURDATE(), NULL, 'DRAFT', 'Bản nháp cho SV 1', 1, 4, NOW(), NOW()),
('CERT-DRAFT-002', CURDATE(), NULL, 'DRAFT', 'Bản nháp cho SV 2', 2, 4, NOW(), NOW()),
('CERT-REVOKED-001', DATE_ADD(CURDATE(), INTERVAL -100 DAY), NULL, 'REVOKED', 'Cấp sai thông tin', 3, 4, NOW(), NOW());
