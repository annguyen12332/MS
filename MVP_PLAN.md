# KẾ HOẠCH MVP - PHIÊN BẢN TỐI THIỂU KHẢ DỤNG

## 🎯 MỤC TIÊU
Hoàn thành **đủ và đúng** các chức năng yêu cầu trong thời gian có hạn của đồ án.

---

## ✅ CHỨC NĂNG BẮT BUỘC (THEO YÊU CẦU)

### 1. QUẢN LÝ THÔNG TIN KHÓA HỌC ⭐⭐⭐
- [ ] Tạo/Sửa/Xóa khóa học
- [ ] Thông tin: tên, nội dung đào tạo, thời lượng, học phí
- [ ] Thời gian tổ chức (bắt đầu/kết thúc)
- [ ] Phân công giảng viên phụ trách

**Database cần:** `courses`, `course_types`, `users` (giảng viên)

---

### 2. QUẢN LÝ ĐĂNG KÝ HỌC ⭐⭐⭐
- [ ] Form đăng ký cho học viên (trong + ngoài trường)
- [ ] Admin duyệt/từ chối đăng ký
- [ ] Hiển thị danh sách học viên đã đăng ký

**Database cần:** `enrollments`, `users` (học viên)

---

### 3. QUẢN LÝ LỚP HỌC ⭐⭐⭐
- [ ] Tạo lớp từ khóa học
- [ ] Phân công giảng viên cho lớp
- [ ] Lập thời khóa biểu (ngày, giờ, phòng học)

**Database cần:** `classes`, `schedules`

---

### 4. QUẢN LÝ QUÁ TRÌNH HỌC TẬP ⭐⭐⭐

#### 4.1 Điểm danh
- [ ] Giảng viên điểm danh theo buổi học
- [ ] Ghi nhận: Có mặt/Vắng/Có phép
- [ ] Xem lịch sử điểm danh

#### 4.2 Nhập điểm
- [ ] Nhập điểm: Chuyên cần, Quá trình, Thi cuối kỳ, Tổng kết
- [ ] Tính điểm tự động
- [ ] Xếp loại: Đạt/Không đạt

#### 4.3 Đánh giá kết quả
- [ ] Hiển thị bảng điểm
- [ ] Danh sách đạt/không đạt

**Database cần:** `attendances`, `grades`

---

### 5. CẤP CHỨNG CHỈ ⭐⭐⭐
- [ ] Điều kiện: Điểm đạt + Điểm danh đủ %
- [ ] Tạo danh sách đủ điều kiện
- [ ] Cấp chứng chỉ (ghi nhận số, ngày cấp)
- [ ] **Không bắt buộc:** Export PDF (có thể làm sau)

**Database cần:** `certificates`

---

### 6. THỐNG KÊ & BÁO CÁO ⭐⭐⭐
- [ ] Số lượng học viên theo khóa/lớp
- [ ] Tỷ lệ điểm danh
- [ ] Kết quả học tập (% đạt/không đạt)
- [ ] Doanh thu theo khóa học
- [ ] **Không bắt buộc:** Export Excel/PDF (có thể làm sau)

**Chỉ cần:** Views hiển thị số liệu, biểu đồ đơn giản

---

## 👥 PHÂN QUYỀN NGƯỜI DÙNG

### Admin (Quản trị viên)
- ✅ Quản lý khóa học (CRUD)
- ✅ Quản lý lớp học (CRUD)
- ✅ Quản lý giảng viên (CRUD)
- ✅ Quản lý học viên (CRUD)
- ✅ Duyệt đăng ký học
- ✅ Cấp chứng chỉ
- ✅ Xem thống kê, báo cáo

### Giảng viên (Teacher)
- ✅ Xem lịch dạy
- ✅ Điểm danh học viên
- ✅ Nhập điểm

### Học viên (Student)
- ✅ Đăng ký khóa học
- ✅ Xem lịch học
- ✅ Xem tài liệu (nếu có)
- ✅ Xem điểm

---

## 🗄️ DATABASE TỐI GIẢN

### Bảng CẦN THIẾT (9 bảng)

```
1. users                 # Tất cả người dùng (admin, teacher, student)
2. course_types          # Loại khóa học (CNTT, NN, KNM, SP)
3. courses               # Khóa học
4. classes               # Lớp học
5. enrollments           # Đăng ký học
6. schedules             # Thời khóa biểu
7. attendances           # Điểm danh
8. grades                # Điểm số
9. certificates          # Chứng chỉ
```

### Bảng BỔ SUNG (Nếu còn thời gian)

```
10. course_materials     # Tài liệu học tập
11. notifications        # Thông báo
12. payments             # Thanh toán (nếu yêu cầu quản lý học phí)
```

---

## 🚫 CHỨC NĂNG KHÔNG ƯU TIÊN (Làm sau nếu còn thời gian)

- ❌ Tích hợp AI/Chatbot
- ❌ Thanh toán online
- ❌ Email/SMS tự động
- ❌ Mobile app
- ❌ Export PDF chứng chỉ (có thể chỉ ghi nhận thông tin)
- ❌ Dashboard nâng cao với biểu đồ phức tạp
- ❌ Tài liệu upload/download (làm sau)

---

## 📅 LỘ TRÌNH PHÁT TRIỂN (8-10 TUẦN)

### TUẦN 1-2: Setup & Database
- [ ] Setup Spring Boot project
- [ ] Cấu hình MySQL
- [ ] Tạo Entity classes (JPA)
- [ ] Tạo database migrations
- [ ] Seed data mẫu

### TUẦN 3-4: Authentication & Admin CRUD
- [ ] Spring Security + Login/Logout
- [ ] Phân quyền (Admin/Teacher/Student)
- [ ] Admin: CRUD Khóa học
- [ ] Admin: CRUD Lớp học
- [ ] Admin: CRUD Người dùng

### TUẦN 5-6: Enrollment & Schedule
- [ ] Học viên đăng ký khóa học
- [ ] Admin duyệt đăng ký
- [ ] Lập thời khóa biểu
- [ ] Xem lịch học/lịch dạy

### TUẦN 7-8: Attendance & Grading
- [ ] Giảng viên điểm danh
- [ ] Giảng viên nhập điểm
- [ ] Tính điểm tự động
- [ ] Xếp loại Đạt/Không đạt

### TUẦN 9: Certificates & Reports
- [ ] Xác định học viên đủ điều kiện
- [ ] Cấp chứng chỉ
- [ ] Thống kê cơ bản
- [ ] Dashboard cho 3 vai trò

### TUẦN 10: Testing & Documentation
- [ ] Test các chức năng chính
- [ ] Fix bugs
- [ ] Viết tài liệu hướng dẫn sử dụng
- [ ] Chuẩn bị báo cáo đồ án

---

## 🎨 UI/UX ĐƠN GIẢN

### Giao diện sử dụng:
- **Thymeleaf** + **Bootstrap 5**
- Template có sẵn: AdminLTE / SB Admin 2 (free)
- Không cần thiết kế UI phức tạp
- Ưu tiên chức năng hoạt động đúng

### Màn hình chính cần có:

**Admin:**
1. Dashboard (thống kê tổng quan)
2. Quản lý khóa học (list, create, edit, delete)
3. Quản lý lớp học (list, create, edit, delete, assign teacher)
4. Quản lý người dùng (list, create, edit, delete)
5. Duyệt đăng ký (list enrollments, approve/reject)
6. Thời khóa biểu (create schedule)
7. Cấp chứng chỉ (list eligible students, issue)
8. Báo cáo (enrollment stats, attendance stats, grades)

**Teacher:**
1. Dashboard
2. Lịch dạy (my schedule)
3. Điểm danh (attendance form)
4. Nhập điểm (grading form)

**Student:**
1. Dashboard
2. Khóa học khả dụng (available courses)
3. Đăng ký (enrollment form)
4. Lịch học (my schedule)
5. Xem điểm (my grades)
6. Tài liệu (nếu có)

---

## 🔧 CÔNG NGHỆ STACK

```yaml
Backend:
  Framework: Spring Boot 3.2.x
  Java: 17 hoặc 21
  ORM: Spring Data JPA + Hibernate
  Security: Spring Security
  Validation: Bean Validation

Frontend:
  Template: Thymeleaf
  CSS: Bootstrap 5.3
  JS: jQuery, Chart.js (cho báo cáo)
  Icons: Font Awesome

Database:
  DBMS: MySQL 8.0+
  Migration: Không cần (dùng JPA auto-create ban đầu)

Tools:
  IDE: IntelliJ IDEA / Eclipse / VS Code
  Build: Maven
  Server: Embedded Tomcat
  Testing: JUnit 5, Mockito
```

---

## ✅ TIÊU CHÍ HOÀN THÀNH MVP

### Chức năng (70%)
- ✅ Tất cả CRUD operations hoạt động
- ✅ Phân quyền 3 vai trò chính xác
- ✅ Quy trình: Tạo khóa → Tạo lớp → Đăng ký → Điểm danh → Nhập điểm → Cấp chứng chỉ
- ✅ Thống kê hiển thị đúng dữ liệu

### Giao diện (20%)
- ✅ Responsive cơ bản
- ✅ Dễ sử dụng, rõ ràng
- ✅ Form validation (frontend + backend)

### Kỹ thuật (10%)
- ✅ Code sạch, có comment
- ✅ Database chuẩn hóa
- ✅ Không có lỗi nghiêm trọng
- ✅ Có thể demo được

---

## 📝 CHECKLIST TRƯỚC KHI NỘP

- [ ] Tất cả 6 chức năng chính hoạt động
- [ ] 3 vai trò đăng nhập được và thấy đúng giao diện
- [ ] Database có dữ liệu mẫu để demo
- [ ] Tài liệu hướng dẫn cài đặt (README.md)
- [ ] Tài liệu hướng dẫn sử dụng (USER_GUIDE.md)
- [ ] Báo cáo đồ án (Use case, Class diagram, Database design)
- [ ] Source code sạch, có comment
- [ ] Video demo (nếu yêu cầu)

---

## 🚀 TIP ĐỂ HOÀN THÀNH NHANH

1. **Sử dụng Spring Initializr** để tạo project nhanh
2. **Copy template Bootstrap** (AdminLTE) thay vì tự design
3. **Tạo Base Entity, Base Controller** để tái sử dụng code
4. **Ưu tiên backend logic** trước, UI sau
5. **Test từng module** khi hoàn thành, đừng để cuối cùng
6. **Git commit thường xuyên** để backup
7. **Hỏi AI/ChatGPT** khi gặp lỗi Spring Boot (tiết kiệm thời gian debug)

---

**File tiếp theo:** [SPRINGBOOT_SETUP.md](SPRINGBOOT_SETUP.md) - Hướng dẫn setup chi tiết
