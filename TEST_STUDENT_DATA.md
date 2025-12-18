# Hướng dẫn kiểm tra và test chức năng Student Browse Classes

## Vấn đề hiện tại
Trang `http://localhost:8080/student/enrollments/browse` không hiển thị danh sách lớp học hoặc dropdown khóa học.

## Nguyên nhân có thể xảy ra

### 1. Không có dữ liệu trong database
Cần kiểm tra xem có:
- **Courses** với `status = 'ACTIVE'`
- **Classes** với `status = 'PENDING'` và `currentStudents < maxStudents`

### 2. Cách kiểm tra dữ liệu

#### Kiểm tra Courses
```sql
-- Xem tất cả courses
SELECT id, code, name, status FROM courses;

-- Xem courses ACTIVE
SELECT id, code, name, status FROM courses WHERE status = 'ACTIVE';
```

#### Kiểm tra Classes
```sql
-- Xem tất cả classes
SELECT id, class_code, class_name, status, current_students, max_students
FROM classes;

-- Xem classes đang mở đăng ký (PENDING và còn chỗ)
SELECT c.id, c.class_code, c.class_name, co.name as course_name,
       c.current_students, c.max_students, c.status
FROM classes c
JOIN courses co ON c.course_id = co.id
WHERE c.status = 'PENDING'
  AND c.current_students < c.max_students;
```

## Giải pháp: Thêm dữ liệu test

### Bước 1: Tạo Courses
```sql
-- Tạo một số khóa học mẫu (nếu chưa có)
INSERT INTO courses (code, name, description, duration_hours, status, created_at)
VALUES
('JAVA-001', 'Lập trình Java cơ bản', 'Khóa học Java cho người mới bắt đầu', 40, 'ACTIVE', CURRENT_TIMESTAMP),
('PYTHON-001', 'Lập trình Python', 'Khóa học Python từ cơ bản đến nâng cao', 35, 'ACTIVE', CURRENT_TIMESTAMP),
('WEB-001', 'Phát triển Web với Spring Boot', 'Khóa học xây dựng ứng dụng web', 50, 'ACTIVE', CURRENT_TIMESTAMP);
```

### Bước 2: Tạo Classes
```sql
-- Lấy ID của course vừa tạo (hoặc course đã có)
-- Giả sử course_id = 1

INSERT INTO classes (class_code, class_name, course_id, start_date, end_date,
                     max_students, current_students, status, created_at)
VALUES
('JAVA-001-2024-1', 'Lớp Java K1 - 2024', 1, '2024-12-10', '2025-01-15', 30, 0, 'PENDING', CURRENT_TIMESTAMP),
('JAVA-001-2024-2', 'Lớp Java K2 - 2024', 1, '2024-12-15', '2025-01-20', 25, 5, 'PENDING', CURRENT_TIMESTAMP),
('PYTHON-001-2024-1', 'Lớp Python K1', 2, '2024-12-12', '2025-01-18', 20, 3, 'PENDING', CURRENT_TIMESTAMP);
```

### Bước 3: Gán giảng viên (optional)
```sql
-- Tìm user có role TEACHER
SELECT id, username, full_name, role FROM users WHERE role = 'TEACHER';

-- Gán giảng viên cho lớp (giả sử teacher_id = 2)
UPDATE classes SET teacher_id = 2 WHERE id IN (1, 2, 3);
```

## Kiểm tra sau khi thêm dữ liệu

### 1. Test query trong code
Controller sử dụng:
```java
courseService.findActiveCourses()
// -> SELECT * FROM courses WHERE status = 'ACTIVE'

classService.findAvailableClasses()
// -> SELECT * FROM classes WHERE status = 'PENDING' AND current_students < max_students
```

### 2. Kiểm tra trên UI

1. **Khởi động lại ứng dụng**:
   ```bash
   ./mvnw spring-boot:run
   ```

2. **Đăng nhập với tài khoản STUDENT**

3. **Truy cập**: `http://localhost:8080/student/enrollments/browse`

4. **Kiểm tra**:
   - ✅ Dropdown "Lọc theo khóa học" hiển thị danh sách khóa học
   - ✅ Hiển thị text "Có X khóa học" dưới dropdown
   - ✅ Grid hiển thị các card lớp học
   - ✅ Nút "Đăng ký" hoạt động khi click

### 3. Test chức năng filter

1. **Chọn khóa học từ dropdown** → URL thay đổi thành:
   ```
   http://localhost:8080/student/enrollments/browse?courseId=1
   ```

2. **Kết quả**: Chỉ hiển thị các lớp học thuộc khóa học đã chọn

3. **Click "Đặt lại"** → Quay về xem tất cả lớp học

### 4. Test chức năng đăng ký

1. **Click nút "Đăng ký"** trên một lớp học
2. **Modal xuất hiện** với thông tin lớp học
3. **Nhập ghi chú** (optional)
4. **Click "Xác nhận đăng ký"**
5. **Kết quả**:
   - Redirect về `/student/dashboard`
   - Hiển thị thông báo "Đăng ký thành công! Vui lòng chờ duyệt."
   - Enrollment được tạo với status = PENDING

## Debug nếu vẫn không hoạt động

### Check 1: Xem log console
```
# Trong console khi start app, tìm:
- Errors liên quan đến database connection
- Errors trong controller methods
```

### Check 2: Access API trực tiếp
```bash
# Kiểm tra xem có lỗi 500 hay 403 không
curl -i http://localhost:8080/student/enrollments/browse
```

### Check 3: Kiểm tra SecurityConfig
File: `src/main/java/com/nute/training/config/SecurityConfig.java`

Đảm bảo có permit cho student:
```java
.requestMatchers("/student/**").hasRole("STUDENT")
```

### Check 4: Kiểm tra template được load
Trong page source (View Page Source), tìm:
```html
<!-- Debug info -->
<small class="text-muted d-block mt-1">
    Có <strong>X</strong> khóa học
</small>
```

Nếu thấy "Có 0 khóa học" → Database không có courses ACTIVE
Nếu không thấy dòng này → Template không được render đúng

## Script tạo dữ liệu đầy đủ

```sql
-- 1. Tạo courses
INSERT INTO courses (code, name, description, duration_hours, status, created_at)
VALUES
('JAVA-001', 'Lập trình Java cơ bản', 'Khóa học Java cho người mới bắt đầu', 40, 'ACTIVE', CURRENT_TIMESTAMP),
('PYTHON-001', 'Lập trình Python', 'Khóa học Python từ cơ bản đến nâng cao', 35, 'ACTIVE', CURRENT_TIMESTAMP),
('WEB-001', 'Phát triển Web với Spring Boot', 'Khóa học xây dựng ứng dụng web', 50, 'ACTIVE', CURRENT_TIMESTAMP);

-- 2. Tạo classes (thay course_id phù hợp)
INSERT INTO classes (class_code, class_name, course_id, start_date, end_date,
                     max_students, current_students, status, location, created_at)
VALUES
('JAVA-001-2024-1', 'Lớp Java K1 - 2024', 1, '2024-12-10', '2025-01-15', 30, 0, 'PENDING', 'Phòng A101', CURRENT_TIMESTAMP),
('JAVA-001-2024-2', 'Lớp Java K2 - 2024', 1, '2024-12-15', '2025-01-20', 25, 5, 'PENDING', 'Phòng A102', CURRENT_TIMESTAMP),
('PYTHON-001-2024-1', 'Lớp Python K1', 2, '2024-12-12', '2025-01-18', 20, 3, 'PENDING', 'Phòng B201', CURRENT_TIMESTAMP),
('WEB-001-2024-1', 'Lớp Web Development K1', 3, '2024-12-20', '2025-02-10', 35, 0, 'PENDING', 'Phòng C301', CURRENT_TIMESTAMP);

-- 3. Gán giảng viên (nếu có)
-- Tìm teacher_id trước
SELECT id, username, full_name FROM users WHERE role = 'TEACHER' LIMIT 1;

-- Gán (thay teacher_id phù hợp)
UPDATE classes SET teacher_id = 2 WHERE id IN (1, 2, 3, 4);

-- 4. Verify
SELECT c.class_code, c.class_name, co.name as course,
       c.current_students, c.max_students, c.status,
       u.full_name as teacher
FROM classes c
JOIN courses co ON c.course_id = co.id
LEFT JOIN users u ON c.teacher_id = u.id
WHERE c.status = 'PENDING' AND c.current_students < c.max_students;
```

## Kết quả mong đợi

Sau khi thêm dữ liệu và reload trang:

✅ Dropdown hiển thị: "Có 3 khóa học"
✅ Grid hiển thị 4 lớp học với đầy đủ thông tin
✅ Chọn "Lập trình Java cơ bản" → Chỉ hiển thị 2 lớp Java
✅ Click "Đăng ký" → Modal hiện lên
✅ Submit form → Tạo enrollment thành công

## Liên hệ

Nếu vẫn gặp vấn đề, cung cấp:
1. Screenshot của trang browse
2. Output của các query SQL ở trên
3. Log lỗi trong console (nếu có)
