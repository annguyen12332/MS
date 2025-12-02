# 🎨 HƯỚNG DẪN CẬP NHẬT THIẾT KẾ MỚI

## 📋 TỔNG QUAN

Hệ thống đã được thiết kế lại hoàn toàn với giao diện hiện đại, chuyên nghiệp và responsive.

### ✨ Các File Đã Tạo Mới

1. **CSS Modern**: `/src/main/resources/static/css/modern-style.css`
   - Hệ thống CSS hoàn chỉnh với variables, components, utilities
   - Responsive design cho mobile/tablet/desktop
   - Animations và transitions mượt mà

2. **Layout Mới**: `/src/main/resources/templates/layout/modern-layout.html`
   - Layout thống nhất cho toàn hệ thống
   - Sidebar gradient đẹp với navigation icons
   - Top navbar với breadcrumb và user info
   - Flash messages tự động ẩn sau 5 giây

3. **Trang Login Mới**: `/src/main/resources/templates/auth/login.html`
   - Thiết kế gradient background
   - Form đăng nhập đẹp với icons
   - Animations fade-in/slide-down

4. **Dashboard Admin Mới**: `/src/main/resources/templates/admin/dashboard.html`
   - Stat cards với gradient màu sắc
   - Tables hiện đại với hover effects
   - Responsive grid layout

---

## 🚀 CÁCH SỬ DỤNG

### Bước 1: Áp dụng Layout Mới cho Trang

Thay đổi tất cả các trang HTML từ:
```html
layout:decorate="~{layout/material-layout}"
```

Thành:
```html
layout:decorate="~{layout/modern-layout}"
```

### Bước 2: Cập Nhật Các Trang Còn Lại

#### Student Dashboard
```html
<!DOCTYPE html>
<html lang="vi" xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layout/modern-layout}">
<head>
    <title>Student Dashboard</title>
</head>
<body>
<div layout:fragment="content">
    <!-- Page Header -->
    <div class="page-header mb-4">
        <h1 class="page-title">Dashboard Học viên</h1>
        <p class="page-subtitle">Chào mừng trở lại!</p>
    </div>

    <!-- Stats Cards -->
    <div class="row mb-4">
        <div class="col-4">
            <div class="stat-card primary">
                <div class="stat-card-label">Khóa học đang học</div>
                <div class="stat-card-value" th:text="${activeCoursesCount} ?: '0'">0</div>
                <i class="fas fa-book-reader stat-card-icon"></i>
            </div>
        </div>
        <!-- Thêm các stat cards khác tương tự -->
    </div>

    <!-- Content tables/lists -->
    <div class="card">
        <div class="card-header">Lịch học sắp tới</div>
        <div class="card-body" style="padding: 0;">
            <div class="table-wrapper">
                <table class="table">
                    <!-- Table content -->
                </table>
            </div>
        </div>
    </div>
</div>
</body>
</html>
```

#### Teacher Dashboard
Tương tự như Student Dashboard, thay đổi tiêu đề và nội dung phù hợp.

---

## 🎨 CÁC COMPONENTS CHÍNH

### 1. Stat Cards
```html
<!-- Primary (Blue) -->
<div class="stat-card primary">
    <div class="stat-card-label">Tiêu đề</div>
    <div class="stat-card-value">123</div>
    <div class="stat-card-footer">Mô tả</div>
    <i class="fas fa-icon stat-card-icon"></i>
</div>

<!-- Success (Green) -->
<div class="stat-card success">...</div>

<!-- Warning (Orange) -->
<div class="stat-card warning">...</div>

<!-- Info (Light Blue) -->
<div class="stat-card info">...</div>
```

### 2. Cards
```html
<div class="card">
    <div class="card-header">
        <span><i class="fas fa-icon me-2 text-primary"></i>Tiêu đề</span>
        <a href="#" class="btn btn-sm btn-primary">Action</a>
    </div>
    <div class="card-body">
        Nội dung
    </div>
    <div class="card-footer">
        Footer (optional)
    </div>
</div>
```

### 3. Tables
```html
<div class="table-wrapper">
    <table class="table">
        <thead>
            <tr>
                <th>Cột 1</th>
                <th>Cột 2</th>
                <th class="text-center">Cột 3</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>Dữ liệu</td>
                <td>Dữ liệu</td>
                <td class="text-center">Dữ liệu</td>
            </tr>
        </tbody>
    </table>
</div>
```

### 4. Buttons
```html
<!-- Primary -->
<button class="btn btn-primary">Button</button>

<!-- Success -->
<button class="btn btn-success">Button</button>

<!-- Danger -->
<button class="btn btn-danger">Button</button>

<!-- Outline -->
<button class="btn btn-outline">Button</button>

<!-- Sizes -->
<button class="btn btn-primary btn-sm">Small</button>
<button class="btn btn-primary">Normal</button>
<button class="btn btn-primary btn-lg">Large</button>
```

### 5. Badges
```html
<span class="badge badge-primary">Primary</span>
<span class="badge badge-success">Success</span>
<span class="badge badge-warning">Warning</span>
<span class="badge badge-danger">Danger</span>
<span class="badge badge-info">Info</span>
```

### 6. Alerts
```html
<div class="alert alert-success">
    <i class="fas fa-check-circle"></i>
    <span>Thành công!</span>
</div>

<div class="alert alert-danger">
    <i class="fas fa-exclamation-circle"></i>
    <span>Lỗi!</span>
</div>
```

### 7. Forms
```html
<div class="form-group">
    <label class="form-label">Nhãn</label>
    <input type="text" class="form-control" placeholder="Nhập nội dung...">
</div>
```

---

## 📱 RESPONSIVE GRID SYSTEM

```html
<div class="row">
    <div class="col-12">100% width</div>
    <div class="col-6">50% width</div>
    <div class="col-4">33.33% width</div>
    <div class="col-3">25% width</div>
    <div class="col-8">66.66% width</div>
</div>
```

**Responsive:** Tất cả columns tự động chuyển thành 100% width trên mobile (<768px)

---

## 🎯 CÁC TRANG CẦN CẬP NHẬT

### ✅ Đã Hoàn Thành
- [x] Login Page
- [x] Admin Dashboard
- [x] Layout chính (modern-layout.html)
- [x] CSS System

### ⏳ Cần Cập Nhật
- [ ] Student Dashboard (`/templates/student/dashboard.html`)
- [ ] Teacher Dashboard (`/templates/teacher/dashboard.html`)
- [ ] Register Page (`/templates/auth/register.html`)
- [ ] Courses List (`/templates/admin/courses/list.html`)
- [ ] Courses Form (`/templates/admin/courses/form.html`)
- [ ] Classes List (`/templates/admin/classes/list.html`)
- [ ] Classes Form (`/templates/admin/classes/form.html`)
- [ ] Users List (`/templates/admin/users/list.html`)
- [ ] Users Form (`/templates/admin/users/form.html`)
- [ ] Enrollments List (`/templates/admin/enrollments/list.html`)
- [ ] Certificates List (`/templates/admin/certificates/list.html`)
- [ ] Profile View (`/templates/profile/view.html`)
- [ ] Error Pages (`/templates/error/403.html`, `404.html`, `500.html`)

---

## 💡 HƯỚNG DẪN CẬP NHẬT TỪNG TRANG

### Bước 1: Thay Layout
```html
<!-- Cũ -->
layout:decorate="~{layout/material-layout}"
<!-- hoặc -->
layout:decorate="~{layout/main-layout}"

<!-- Mới -->
layout:decorate="~{layout/modern-layout}"
```

### Bước 2: Cập Nhật Structure
```html
<div layout:fragment="content">
    <!-- Page Header -->
    <div class="page-header mb-4">
        <h1 class="page-title">Tiêu đề Trang</h1>
        <p class="page-subtitle">Mô tả ngắn</p>
    </div>

    <!-- Content ở đây -->
</div>
```

### Bước 3: Sử Dụng Components Mới
- Thay thế các div bằng `stat-card` cho statistics
- Thay thế tables bằng `table` class mới
- Thay thế buttons bằng `btn` classes mới
- Thay thế badges bằng `badge` classes mới

---

## 🎨 MÀU SẮC HỆ THỐNG

```css
--primary: #4F46E5      /* Indigo */
--success: #10B981      /* Green */
--warning: #F59E0B      /* Amber */
--danger: #EF4444       /* Red */
--info: #3B82F6         /* Blue */
```

---

## 📝 LƯU Ý QUAN TRỌNG

1. **Icons**: Sử dụng Font Awesome 6.5.1
   ```html
   <i class="fas fa-icon-name"></i>
   ```

2. **Spacing**: Sử dụng utility classes
   ```html
   .mb-0, .mb-1, .mb-2, .mb-3, .mb-4, .mb-5
   .mt-0, .mt-1, .mt-2, .mt-3, .mt-4, .mt-5
   ```

3. **Text Alignment**:
   ```html
   .text-left, .text-center, .text-right
   ```

4. **Flex Utilities**:
   ```html
   .d-flex
   .justify-content-between
   .align-items-center
   .gap-1, .gap-2, .gap-3
   ```

---

## 🚀 TEST & DEPLOYMENT

1. **Restart ứng dụng** sau khi cập nhật
2. **Clear browser cache** (Ctrl + Shift + Delete)
3. **Test responsive** trên các kích thước màn hình
4. **Kiểm tra cross-browser** (Chrome, Firefox, Edge, Safari)

---

## 📞 HỖ TRỢ

Nếu có vấn đề:
1. Kiểm tra Console browser (F12) xem có lỗi CSS/JS không
2. Đảm bảo path `/css/modern-style.css` được load đúng
3. Kiểm tra Thymeleaf syntax trong HTML files

---

**Chúc may mắn với thiết kế mới! 🎉**
