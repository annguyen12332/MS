# HƯỚNG DẪN TRIỂN KHAI - Short Term Training Management System

## 📋 MỤC LỤC

1. [Tổng quan hệ thống](#tổng-quan-hệ-thống)
2. [Kiến trúc đã hoàn thành](#kiến-trúc-đã-hoàn-thành)
3. [Controllers cần hoàn thiện](#controllers-cần-hoàn-thiện)
4. [Views/Templates cần tạo](#viewstemplates-cần-tạo)
5. [Testing & Deployment](#testing--deployment)

---

## 🎯 TỔNG QUAN HỆ THỐNG

### Tech Stack
- **Backend:** Spring Boot 3.2.5 + Java 21
- **Database:** MySQL 8.0+
- **Template Engine:** Thymeleaf
- **UI Framework:** AdminLTE 3 (Bootstrap 5)
- **Security:** Spring Security với BCrypt
- **Build Tool:** Maven

### Cấu trúc Package
```
com.nute.training/
├── entity/          # 9 Entity classes ✓ (Hoàn thành)
├── repository/      # 9 Repository interfaces ✓ (Hoàn thành)
├── service/         # 9 Service classes ✓ (Hoàn thành)
├── controller/      # Controllers ⚠️ (Một phần hoàn thành)
│   ├── common/      # AuthController ✓
│   ├── admin/       # AdminDashboard, Course, Class, Enrollment ✓
│   ├── teacher/     # TeacherAttendance ✓
│   └── student/     # ❌ Cần hoàn thiện
├── config/          # SecurityConfig ✓
├── util/            # AuthenticationHelper ✓
└── dto/             # ❌ Cần tạo (nếu cần)
```

---

## ✅ KIẾN TRÚC ĐÃ HOÀN THÀNH

### 1. Entity Layer (9 classes)

| Entity | File | Chức năng | Business Rules |
|--------|------|-----------|----------------|
| **User** | [User.java](src/main/java/com/nute/training/entity/User.java) | Người dùng (Admin/Teacher/Student) | - Password BCrypt<br>- Role & Status enum<br>- Validation (email, username unique) |
| **CourseType** | [CourseType.java](src/main/java/com/nute/training/entity/CourseType.java) | Loại khóa học (CNTT, NN, KNM, SP) | - Code unique |
| **Course** | [Course.java](src/main/java/com/nute/training/entity/Course.java) | Khóa học | - Code unique<br>- Duration, tuition fee, max students validation |
| **ClassEntity** | [ClassEntity.java](src/main/java/com/nute/training/entity/ClassEntity.java) | Lớp học | - Date range validation<br>- isFull() method<br>- currentStudents tracking |
| **Enrollment** | [Enrollment.java](src/main/java/com/nute/training/entity/Enrollment.java) | Đăng ký học | - Unique (student, class)<br>- Payment status tracking<br>- Approval workflow |
| **Schedule** | [Schedule.java](src/main/java/com/nute/training/entity/Schedule.java) | Thời khóa biểu | - Time range validation<br>- Unique (class, session_number) |
| **Attendance** | [Attendance.java](src/main/java/com/nute/training/entity/Attendance.java) | Điểm danh | - Unique (schedule, student)<br>- isCountedForAttendanceScore() |
| **Grade** | [Grade.java](src/main/java/com/nute/training/entity/Grade.java) | Điểm số | - Auto calculate: total, letter, pass<br>- Score formula: 10% + 30% + 60% |
| **Certificate** | [Certificate.java](src/main/java/com/nute/training/entity/Certificate.java) | Chứng chỉ | - Eligibility validation<br>- Auto generate code |

### 2. Service Layer (9 classes)

Tất cả Service classes đã có đầy đủ business logic và validation. Xem chi tiết tại:
- [UserService.java](src/main/java/com/nute/training/service/UserService.java)
- [CourseService.java](src/main/java/com/nute/training/service/CourseService.java)
- [ClassService.java](src/main/java/com/nute/training/service/ClassService.java)
- [EnrollmentService.java](src/main/java/com/nute/training/service/EnrollmentService.java)
- [ScheduleService.java](src/main/java/com/nute/training/service/ScheduleService.java)
- [AttendanceService.java](src/main/java/com/nute/training/service/AttendanceService.java)
- [GradeService.java](src/main/java/com/nute/training/service/GradeService.java)
- [CertificateService.java](src/main/java/com/nute/training/service/CertificateService.java)
- [CourseTypeService.java](src/main/java/com/nute/training/service/CourseTypeService.java)

### 3. Controllers Đã Hoàn Thành

| Controller | File | Chức năng chính |
|------------|------|-----------------|
| **AuthController** | [AuthController.java](src/main/java/com/nute/training/controller/common/AuthController.java) | Login, Logout, Dashboard redirect |
| **AdminDashboardController** | [AdminDashboardController.java](src/main/java/com/nute/training/controller/admin/AdminDashboardController.java) | Thống kê tổng quan |
| **AdminCourseController** | [AdminCourseController.java](src/main/java/com/nute/training/controller/admin/AdminCourseController.java) | CRUD khóa học |
| **AdminClassController** | [AdminClassController.java](src/main/java/com/nute/training/controller/admin/AdminClassController.java) | CRUD lớp học, Phân công GV |
| **AdminEnrollmentController** | [AdminEnrollmentController.java](src/main/java/com/nute/training/controller/admin/AdminEnrollmentController.java) | Duyệt/Từ chối đăng ký |
| **TeacherAttendanceController** | [TeacherAttendanceController.java](src/main/java/com/nute/training/controller/teacher/TeacherAttendanceController.java) | Điểm danh học viên |

---

## 🔨 CONTROLLERS CẦN HOÀN THIỆN

### 📌 1. AdminUserController

**File:** `src/main/java/com/nute/training/controller/admin/AdminUserController.java`

**Mục đích:** Quản lý người dùng (Admin/Teacher/Student)

**Các method cần có:**

```java
@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    // GET /admin/users - Danh sách user (có filter theo role)
    @GetMapping
    public String list(@RequestParam(required = false) String role, Model model)

    // GET /admin/users/create - Form tạo user
    @GetMapping("/create")
    public String createForm(Model model)

    // POST /admin/users/create - Xử lý tạo user
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute User user, BindingResult result)

    // GET /admin/users/{id}/edit - Form sửa user
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model)

    // POST /admin/users/{id}/update - Xử lý cập nhật user
    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id, @ModelAttribute User user)

    // POST /admin/users/{id}/change-status - Thay đổi trạng thái
    @PostMapping("/{id}/change-status")
    public String changeStatus(@PathVariable Long id, @RequestParam User.Status status)

    // POST /admin/users/{id}/reset-password - Reset mật khẩu
    @PostMapping("/{id}/reset-password")
    public String resetPassword(@PathVariable Long id, @RequestParam String newPassword)

    // POST /admin/users/{id}/delete - Xóa user
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id)
}
```

**Business Logic quan trọng:**
- Validate username và email unique khi tạo/sửa
- Mã hóa password với BCrypt
- Không cho xóa user đang có dữ liệu liên quan
- Chỉ ADMIN mới được thay đổi role của user khác

---

### 📌 2. AdminScheduleController

**File:** `src/main/java/com/nute/training/controller/admin/AdminScheduleController.java`

**Mục đích:** Lập thời khóa biểu cho lớp học

**Các method cần có:**

```java
@Controller
@RequestMapping("/admin/schedules")
@RequiredArgsConstructor
public class AdminScheduleController {

    private final ScheduleService scheduleService;
    private final ClassService classService;

    // GET /admin/schedules/class/{classId} - Xem TKB của lớp
    @GetMapping("/class/{classId}")
    public String viewSchedules(@PathVariable Long classId, Model model)

    // GET /admin/schedules/class/{classId}/create - Form tạo lịch học
    @GetMapping("/class/{classId}/create")
    public String createForm(@PathVariable Long classId, Model model)

    // POST /admin/schedules/create - Xử lý tạo lịch
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute Schedule schedule)

    // GET /admin/schedules/{id}/edit - Form sửa lịch
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model)

    // POST /admin/schedules/{id}/update - Cập nhật lịch
    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id, @ModelAttribute Schedule schedule)

    // POST /admin/schedules/{id}/delete - Xóa lịch
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id)

    // POST /admin/schedules/{id}/complete - Đánh dấu hoàn thành
    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id)

    // POST /admin/schedules/{id}/cancel - Hủy buổi học
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id)
}
```

**Business Logic quan trọng:**
- Validate time range (endTime > startTime)
- Kiểm tra xung đột lịch (cùng phòng, cùng thời gian)
- Session number phải unique trong lớp
- Tự động đánh số buổi học

---

### 📌 3. AdminGradeController

**File:** `src/main/java/com/nute/training/controller/admin/AdminGradeController.java`

**Mục đích:** Quản lý điểm số, xem kết quả học tập

**Các method cần có:**

```java
@Controller
@RequestMapping("/admin/grades")
@RequiredArgsConstructor
public class AdminGradeController {

    private final GradeService gradeService;
    private final ClassService classService;
    private final EnrollmentService enrollmentService;

    // GET /admin/grades/class/{classId} - Xem bảng điểm lớp
    @GetMapping("/class/{classId}")
    public String viewClassGrades(@PathVariable Long classId, Model model)

    // GET /admin/grades/class/{classId}/statistics - Thống kê điểm
    @GetMapping("/class/{classId}/statistics")
    public String statistics(@PathVariable Long classId, Model model)

    // GET /admin/grades/student/{studentId} - Xem điểm của học viên
    @GetMapping("/student/{studentId}")
    public String viewStudentGrades(@PathVariable Long studentId, Model model)
}
```

**Lưu ý:**
- Admin chỉ xem, không nhập điểm (giảng viên nhập)
- Hiển thị thống kê: điểm trung bình, tỷ lệ đạt, top học viên

---

### 📌 4. AdminCertificateController

**File:** `src/main/java/com/nute/training/controller/admin/AdminCertificateController.java`

**Mục đích:** Cấp chứng chỉ cho học viên đạt yêu cầu

**Các method cần có:**

```java
@Controller
@RequestMapping("/admin/certificates")
@RequiredArgsConstructor
public class AdminCertificateController {

    private final CertificateService certificateService;
    private final ClassService classService;

    // GET /admin/certificates - Danh sách chứng chỉ đã cấp
    @GetMapping
    public String list(Model model)

    // GET /admin/certificates/class/{classId}/eligible - Học viên đủ điều kiện
    @GetMapping("/class/{classId}/eligible")
    public String eligibleStudents(@PathVariable Long classId, Model model)

    // POST /admin/certificates/issue-batch - Cấp hàng loạt
    @PostMapping("/issue-batch")
    public String issueBatch(@RequestParam Long classId, @RequestParam String codePrefix)

    // POST /admin/certificates/{id}/issue - Cấp đơn lẻ
    @PostMapping("/{id}/issue")
    public String issueSingle(@PathVariable Long id)

    // POST /admin/certificates/{id}/revoke - Thu hồi
    @PostMapping("/{id}/revoke")
    public String revoke(@PathVariable Long id, @RequestParam String reason)

    // GET /admin/certificates/{id} - Xem chi tiết
    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model)
}
```

**Business Logic quan trọng:**
- Kiểm tra điều kiện: grade.pass = true, chưa có certificate
- Auto-generate certificate code theo format: CERT-CLASSID-XXX
- Ghi nhận ngày cấp, người cấp

---

### 📌 5. TeacherDashboardController

**File:** `src/main/java/com/nute/training/controller/teacher/TeacherDashboardController.java`

**Mục đích:** Dashboard cho giảng viên

```java
@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
public class TeacherDashboardController {

    private final ClassService classService;
    private final ScheduleService scheduleService;
    private final AuthenticationHelper authenticationHelper;

    // GET /teacher/dashboard
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User currentTeacher = authenticationHelper.getCurrentUser().orElseThrow();

        // Lớp đang dạy
        var ongoingClasses = classService.findOngoingClassesByTeacher(currentTeacher);

        // Lịch dạy hôm nay
        var todaySchedules = scheduleService.findTeacherScheduleByDate(
            currentTeacher.getId(), LocalDate.now()
        );

        // Lịch sắp tới (7 ngày)
        var upcomingSchedules = scheduleService.findTeacherScheduleByDateRange(
            currentTeacher.getId(),
            LocalDate.now(),
            LocalDate.now().plusDays(7)
        );

        model.addAttribute("ongoingClasses", ongoingClasses);
        model.addAttribute("todaySchedules", todaySchedules);
        model.addAttribute("upcomingSchedules", upcomingSchedules);

        return "teacher/dashboard";
    }
}
```

---

### 📌 6. TeacherGradeController

**File:** `src/main/java/com/nute/training/controller/teacher/TeacherGradeController.java`

**Mục đích:** Giảng viên nhập điểm cho học viên

```java
@Controller
@RequestMapping("/teacher/grades")
@RequiredArgsConstructor
public class TeacherGradeController {

    private final GradeService gradeService;
    private final ClassService classService;
    private final EnrollmentService enrollmentService;
    private final AttendanceService attendanceService;
    private final AuthenticationHelper authenticationHelper;

    // GET /teacher/grades/class/{classId} - Xem bảng điểm lớp
    @GetMapping("/class/{classId}")
    public String viewGrades(@PathVariable Long classId, Model model)

    // GET /teacher/grades/class/{classId}/input - Form nhập điểm
    @GetMapping("/class/{classId}/input")
    public String inputForm(@PathVariable Long classId, Model model)

    // POST /teacher/grades/save - Lưu điểm
    @PostMapping("/save")
    public String saveGrade(
        @RequestParam Long enrollmentId,
        @RequestParam BigDecimal attendanceScore,
        @RequestParam BigDecimal processScore,
        @RequestParam BigDecimal finalScore,
        @RequestParam(required = false) String note
    )

    // POST /teacher/grades/calculate-attendance-scores - Tự động tính điểm chuyên cần
    @PostMapping("/class/{classId}/calculate-attendance-scores")
    public String calculateAttendanceScores(@PathVariable Long classId)
}
```

**Business Logic quan trọng:**
- Tự động tính điểm chuyên cần từ tỷ lệ điểm danh
- Auto-calculate total score, grade letter, pass/fail
- Validate điểm 0-10

**Công thức tính điểm chuyên cần từ điểm danh:**
```
attendanceRate = (presentCount + lateCount) / totalSessions * 100
if (attendanceRate >= 90) → attendanceScore = 10
else if (attendanceRate >= 80) → attendanceScore = 8
else if (attendanceRate >= 70) → attendanceScore = 6
else if (attendanceRate >= 60) → attendanceScore = 4
else → attendanceScore = 2
```

---

### 📌 7. StudentDashboardController

**File:** `src/main/java/com/nute/training/controller/student/StudentDashboardController.java`

**Mục đích:** Dashboard cho học viên

```java
@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentDashboardController {

    private final EnrollmentService enrollmentService;
    private final ScheduleService scheduleService;
    private final GradeService gradeService;
    private final CertificateService certificateService;
    private final AuthenticationHelper authenticationHelper;

    // GET /student/dashboard
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User currentStudent = authenticationHelper.getCurrentUser().orElseThrow();

        // Lớp đang học
        var myEnrollments = enrollmentService.findByStudent(currentStudent);

        // Điểm của tôi
        var myGrades = gradeService.findGradesByStudent(currentStudent.getId());

        // Chứng chỉ của tôi
        var myCertificates = certificateService.findCertificatesByStudent(currentStudent.getId());

        model.addAttribute("enrollments", myEnrollments);
        model.addAttribute("grades", myGrades);
        model.addAttribute("certificates", myCertificates);

        return "student/dashboard";
    }
}
```

---

### 📌 8. StudentEnrollmentController

**File:** `src/main/java/com/nute/training/controller/student/StudentEnrollmentController.java`

**Mục đích:** Học viên đăng ký khóa học

```java
@Controller
@RequestMapping("/student/enrollments")
@RequiredArgsConstructor
public class StudentEnrollmentController {

    private final EnrollmentService enrollmentService;
    private final CourseService courseService;
    private final ClassService classService;
    private final AuthenticationHelper authenticationHelper;

    // GET /student/enrollments - Đăng ký của tôi
    @GetMapping
    public String myEnrollments(Model model)

    // GET /student/enrollments/available-courses - Khóa học có thể đăng ký
    @GetMapping("/available-courses")
    public String availableCourses(Model model)

    // GET /student/enrollments/available-classes - Lớp học có thể đăng ký
    @GetMapping("/available-classes")
    public String availableClasses(@RequestParam(required = false) Long courseId, Model model)

    // POST /student/enrollments/register - Đăng ký lớp học
    @PostMapping("/register")
    public String register(
        @RequestParam Long classId,
        @RequestParam(required = false) String notes
    )

    // POST /student/enrollments/{id}/cancel - Hủy đăng ký
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id)
}
```

**Business Logic quan trọng:**
- Chỉ đăng ký được lớp chưa đầy
- Không đăng ký trùng lớp
- Trạng thái mặc định: PENDING (chờ admin duyệt)

---

### 📌 9. StudentGradeController

**File:** `src/main/java/com/nute/training/controller/student/StudentGradeController.java`

**Mục đích:** Học viên xem điểm của mình

```java
@Controller
@RequestMapping("/student/grades")
@RequiredArgsConstructor
public class StudentGradeController {

    private final GradeService gradeService;
    private final AttendanceService attendanceService;
    private final AuthenticationHelper authenticationHelper;

    // GET /student/grades - Điểm của tôi
    @GetMapping
    public String myGrades(Model model)

    // GET /student/grades/{enrollmentId} - Chi tiết điểm 1 lớp
    @GetMapping("/{enrollmentId}")
    public String gradeDetail(@PathVariable Long enrollmentId, Model model)

    // GET /student/grades/class/{classId}/attendance - Điểm danh của tôi
    @GetMapping("/class/{classId}/attendance")
    public String myAttendance(@PathVariable Long classId, Model model)
}
```

---

## 🎨 VIEWS/TEMPLATES CẦN TẠO

### Cấu trúc thư mục Templates

```
src/main/resources/templates/
├── layout/
│   ├── main.html              # Base layout (AdminLTE 3)
│   ├── admin-sidebar.html     # Sidebar cho Admin
│   ├── teacher-sidebar.html   # Sidebar cho Teacher
│   └── student-sidebar.html   # Sidebar cho Student
├── auth/
│   └── login.html             # Trang đăng nhập
├── error/
│   ├── 403.html              # Access Denied
│   ├── 404.html              # Not Found
│   └── 500.html              # Server Error
├── admin/
│   ├── dashboard.html         # Admin Dashboard
│   ├── courses/
│   │   ├── list.html         # Danh sách khóa học
│   │   ├── form.html         # Form tạo/sửa
│   │   └── view.html         # Chi tiết khóa học
│   ├── classes/
│   │   ├── list.html
│   │   ├── form.html
│   │   └── view.html
│   ├── users/
│   │   ├── list.html
│   │   └── form.html
│   ├── enrollments/
│   │   ├── list.html
│   │   ├── pending.html      # Đăng ký chờ duyệt
│   │   └── list-by-class.html
│   ├── schedules/
│   │   ├── list.html
│   │   └── form.html
│   ├── grades/
│   │   └── class-grades.html
│   └── certificates/
│       ├── list.html
│       └── eligible.html     # Học viên đủ điều kiện
├── teacher/
│   ├── dashboard.html
│   ├── attendance/
│   │   ├── classes.html      # Lớp đang dạy
│   │   ├── schedules.html    # Lịch dạy
│   │   ├── form.html         # Form điểm danh
│   │   └── statistics.html   # Thống kê điểm danh
│   └── grades/
│       ├── list.html         # Bảng điểm
│       └── input.html        # Form nhập điểm
└── student/
    ├── dashboard.html
    ├── enrollments/
    │   ├── my-enrollments.html
    │   ├── available-courses.html
    │   └── available-classes.html
    └── grades/
        ├── my-grades.html
        └── grade-detail.html
```

### Hướng dẫn sử dụng AdminLTE 3

**1. Download AdminLTE 3:**
```bash
# Tải AdminLTE 3 từ: https://adminlte.io/
# Hoặc dùng CDN trong template
```

**2. Base Layout Template (layout/main.html):**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title th:text="${pageTitle} ?: 'Training Management'"></title>

    <!-- AdminLTE CSS -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/admin-lte@3.2/dist/css/adminlte.min.css">
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body class="hold-transition sidebar-mini layout-fixed">
<div class="wrapper">

    <!-- Navbar -->
    <nav class="main-header navbar navbar-expand navbar-white navbar-light">
        <ul class="navbar-nav">
            <li class="nav-item">
                <a class="nav-link" data-widget="pushmenu" href="#"><i class="fas fa-bars"></i></a>
            </li>
        </ul>
        <ul class="navbar-nav ml-auto">
            <li class="nav-item">
                <span class="nav-link" th:text="${currentUser?.fullName}"></span>
            </li>
            <li class="nav-item">
                <form th:action="@{/logout}" method="post" style="display:inline">
                    <button type="submit" class="btn btn-link nav-link">Đăng xuất</button>
                </form>
            </li>
        </ul>
    </nav>

    <!-- Sidebar -->
    <aside class="main-sidebar sidebar-dark-primary elevation-4">
        <a href="#" class="brand-link">
            <span class="brand-text font-weight-light">Training System</span>
        </a>

        <div class="sidebar">
            <!-- Sidebar content based on role -->
            <div th:replace="~{layout/${sidebarFragment} :: sidebar}"></div>
        </div>
    </aside>

    <!-- Content Wrapper -->
    <div class="content-wrapper">
        <!-- Flash messages -->
        <div th:if="${success}" class="alert alert-success alert-dismissible m-3">
            <button type="button" class="close" data-dismiss="alert">&times;</button>
            <span th:text="${success}"></span>
        </div>
        <div th:if="${error}" class="alert alert-danger alert-dismissible m-3">
            <button type="button" class="close" data-dismiss="alert">&times;</button>
            <span th:text="${error}"></span>
        </div>

        <!-- Main content -->
        <div th:replace="~{${contentFragment} :: content}"></div>
    </div>

    <!-- Footer -->
    <footer class="main-footer">
        <strong>Training Management System</strong> - NUTE
    </footer>
</div>

<!-- Scripts -->
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/admin-lte@3.2/dist/js/adminlte.min.js"></script>
</body>
</html>
```

**3. Example Admin Sidebar (layout/admin-sidebar.html):**

```html
<div th:fragment="sidebar">
    <nav class="mt-2">
        <ul class="nav nav-pills nav-sidebar flex-column" data-widget="treeview">
            <li class="nav-item">
                <a th:href="@{/admin/dashboard}" class="nav-link">
                    <i class="nav-icon fas fa-tachometer-alt"></i>
                    <p>Dashboard</p>
                </a>
            </li>
            <li class="nav-item">
                <a th:href="@{/admin/courses}" class="nav-link">
                    <i class="nav-icon fas fa-book"></i>
                    <p>Khóa học</p>
                </a>
            </li>
            <li class="nav-item">
                <a th:href="@{/admin/classes}" class="nav-link">
                    <i class="nav-icon fas fa-chalkboard"></i>
                    <p>Lớp học</p>
                </a>
            </li>
            <li class="nav-item">
                <a th:href="@{/admin/enrollments}" class="nav-link">
                    <i class="nav-icon fas fa-user-plus"></i>
                    <p>Đăng ký học</p>
                </a>
            </li>
            <li class="nav-item">
                <a th:href="@{/admin/users}" class="nav-link">
                    <i class="nav-icon fas fa-users"></i>
                    <p>Người dùng</p>
                </a>
            </li>
            <li class="nav-item">
                <a th:href="@{/admin/certificates}" class="nav-link">
                    <i class="nav-icon fas fa-certificate"></i>
                    <p>Chứng chỉ</p>
                </a>
            </li>
        </ul>
    </nav>
</div>
```

**4. Example Page (admin/courses/list.html):**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" th:replace="~{layout/main :: layout(
    pageTitle='Danh sách khóa học',
    sidebarFragment='admin-sidebar',
    contentFragment=~{:: content}
)}">
<body>
    <div th:fragment="content">
        <section class="content-header">
            <div class="container-fluid">
                <div class="row mb-2">
                    <div class="col-sm-6">
                        <h1>Danh sách khóa học</h1>
                    </div>
                    <div class="col-sm-6">
                        <a th:href="@{/admin/courses/create}" class="btn btn-primary float-right">
                            <i class="fas fa-plus"></i> Tạo mới
                        </a>
                    </div>
                </div>
            </div>
        </section>

        <section class="content">
            <div class="card">
                <div class="card-body">
                    <table class="table table-bordered table-striped">
                        <thead>
                            <tr>
                                <th>Mã</th>
                                <th>Tên khóa học</th>
                                <th>Loại</th>
                                <th>Thời lượng</th>
                                <th>Học phí</th>
                                <th>Trạng thái</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr th:each="course : ${courses}">
                                <td th:text="${course.code}"></td>
                                <td th:text="${course.name}"></td>
                                <td th:text="${course.courseType?.name}"></td>
                                <td th:text="${course.durationHours} + ' giờ'"></td>
                                <td th:text="${#numbers.formatDecimal(course.tuitionFee, 0, 'COMMA', 0, 'POINT')} + ' VNĐ'"></td>
                                <td>
                                    <span th:class="'badge badge-' + ${course.status == 'ACTIVE' ? 'success' : 'secondary'}"
                                          th:text="${course.status}"></span>
                                </td>
                                <td>
                                    <a th:href="@{/admin/courses/{id}(id=${course.id})}" class="btn btn-sm btn-info">
                                        <i class="fas fa-eye"></i>
                                    </a>
                                    <a th:href="@{/admin/courses/{id}/edit(id=${course.id})}" class="btn btn-sm btn-warning">
                                        <i class="fas fa-edit"></i>
                                    </a>
                                    <form th:action="@{/admin/courses/{id}/delete(id=${course.id})}"
                                          method="post" style="display:inline">
                                        <button type="submit" class="btn btn-sm btn-danger"
                                                onclick="return confirm('Xác nhận xóa?')">
                                            <i class="fas fa-trash"></i>
                                        </button>
                                    </form>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </section>
    </div>
</body>
</html>
```

---

## 🧪 TESTING & DEPLOYMENT

### 1. Test Accounts (từ DATABASE_MVP.sql)

| Username | Password | Role | Mô tả |
|----------|----------|------|-------|
| admin | 123456 | ADMIN | Quản trị viên |
| teacher1 | 123456 | TEACHER | Nguyễn Văn A |
| teacher2 | 123456 | TEACHER | Trần Thị B |
| student1 | 123456 | STUDENT | Lê Văn C |
| student2 | 123456 | STUDENT | Phạm Thị D |
| student3 | 123456 | STUDENT | Hoàng Văn E |

### 2. Test Scenarios

**Scenario 1: Quản lý khóa học (Admin)**
1. Login với admin/123456
2. Tạo khóa học mới
3. Sửa thông tin khóa học
4. Thay đổi trạng thái ACTIVE/INACTIVE
5. Xóa khóa học

**Scenario 2: Quy trình đăng ký học**
1. Student login → Đăng ký khóa học
2. Admin login → Duyệt đăng ký
3. Verify currentStudents của lớp tăng lên

**Scenario 3: Quy trình điểm danh**
1. Teacher login → Chọn lớp đang dạy
2. Chọn buổi học → Điểm danh từng học viên
3. Verify attendance records được tạo

**Scenario 4: Quy trình nhập điểm**
1. Teacher login → Chọn lớp
2. Nhập điểm chuyên cần, quá trình, cuối kỳ
3. Verify điểm tổng kết được tự động tính
4. Verify grade letter và pass/fail

**Scenario 5: Cấp chứng chỉ**
1. Admin login → Chọn lớp đã hoàn thành
2. Xem danh sách học viên đủ điều kiện (pass = true)
3. Cấp chứng chỉ hàng loạt
4. Verify certificate code được tạo unique

### 3. Run Application

```bash
# 1. Đảm bảo MySQL đang chạy
mysql -u root -proot

# 2. Chạy ứng dụng
mvn spring-boot:run

# 3. Truy cập
http://localhost:8080/login
```

### 4. Build for Production

```bash
# Build JAR file
mvn clean package

# Run JAR
java -jar target/short-term-training-1.0.0.jar

# Hoặc chạy với profile production
java -jar target/short-term-training-1.0.0.jar --spring.profiles.active=prod
```

---

## 📚 TÀI LIỆU THAM KHẢO

### Spring Boot
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [Thymeleaf Documentation](https://www.thymeleaf.org/documentation.html)

### AdminLTE 3
- [AdminLTE 3 Documentation](https://adminlte.io/docs/3.2/)
- [AdminLTE 3 Examples](https://adminlte.io/themes/v3/index.html)

### Database
- [MySQL Documentation](https://dev.mysql.com/doc/)

---

## 🎯 CHECKLIST HOÀN THIỆN DỰ ÁN

### Backend (Controllers)
- [ ] AdminUserController
- [ ] AdminScheduleController
- [ ] AdminGradeController (view only)
- [ ] AdminCertificateController
- [ ] TeacherDashboardController
- [ ] TeacherGradeController
- [ ] StudentDashboardController
- [ ] StudentEnrollmentController
- [ ] StudentGradeController

### Frontend (Views)
- [ ] Layout templates (main, sidebars)
- [ ] Login page
- [ ] Error pages (403, 404, 500)
- [ ] Admin pages (dashboard, courses, classes, users, enrollments, certificates)
- [ ] Teacher pages (dashboard, attendance, grades)
- [ ] Student pages (dashboard, enrollments, grades)

### Testing
- [ ] Test tất cả CRUD operations
- [ ] Test quy trình đăng ký - duyệt - học - điểm danh - nhập điểm - cấp chứng chỉ
- [ ] Test phân quyền (admin/teacher/student)
- [ ] Test validation & error handling

### Documentation
- [ ] README.md (hướng dẫn cài đặt)
- [ ] USER_GUIDE.md (hướng dẫn sử dụng)
- [ ] Use Case Diagram
- [ ] Class Diagram
- [ ] ERD
- [ ] Video demo

---

## 💡 TIPS & BEST PRACTICES

1. **Code Organization:**
   - Luôn validate input ở Controller và Service layer
   - Sử dụng DTO nếu cần tách biệt Entity và View data
   - Handle exceptions properly với try-catch

2. **Security:**
   - Không bao giờ trả password về frontend
   - Luôn verify quyền truy cập (teacher chỉ xem lớp của mình)
   - Sử dụng CSRF protection

3. **Performance:**
   - Sử dụng FetchType.LAZY cho relationships
   - Index các columns thường query (email, username, code)
   - Pagination cho danh sách lớn

4. **UI/UX:**
   - Flash messages cho mọi action (success/error)
   - Confirm dialog trước khi delete
   - Validation messages rõ ràng

---

**File được tạo bởi:** Claude Code
**Ngày tạo:** 2025-11-30
**Version:** 1.0.0
