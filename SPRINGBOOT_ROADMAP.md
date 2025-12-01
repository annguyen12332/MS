# LỘ TRÌNH PHÁT TRIỂN SPRING BOOT - CHI TIẾT

## 🎯 Mục tiêu
Hoàn thành hệ thống quản lý đào tạo ngắn hạn với Spring Boot + MySQL + Thymeleaf trong 8-10 tuần.

---

## 📅 TUẦN 1-2: SETUP & DATABASE

### Bước 1: Tạo Spring Boot Project

**Sử dụng Spring Initializr (https://start.spring.io/)**

```
Project: Maven
Language: Java
Spring Boot: 3.2.x (stable)
Java: 17 hoặc 21

Packaging: Jar
Dependencies:
  ✅ Spring Web
  ✅ Spring Data JPA
  ✅ MySQL Driver
  ✅ Thymeleaf
  ✅ Spring Security
  ✅ Validation
  ✅ Lombok (optional, nhưng rất hữu ích)
  ✅ Spring Boot DevTools (auto-reload)
```

**Download và import vào IDE**

---

### Bước 2: Cấu hình application.properties

```properties
# src/main/resources/application.properties

# Server
server.port=8080

# Database MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/short_term_training?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Thymeleaf
spring.thymeleaf.cache=false
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Logging
logging.level.org.springframework.security=DEBUG
logging.level.com.nute.training=DEBUG
```

---

### Bước 3: Tạo Database

1. Mở MySQL Workbench hoặc command line
2. Chạy file `DATABASE_MVP.sql` để tạo database + seed data

```bash
mysql -u root -p < DATABASE_MVP.sql
```

---

### Bước 4: Tạo Entity Classes

**Cấu trúc package:**
```
src/main/java/com/nute/training/
├── TrainingApplication.java (main)
├── entity/
│   ├── User.java
│   ├── CourseType.java
│   ├── Course.java
│   ├── ClassEntity.java
│   ├── Enrollment.java
│   ├── Schedule.java
│   ├── Attendance.java
│   ├── Grade.java
│   └── Certificate.java
├── repository/
├── service/
├── controller/
├── config/
└── dto/
```

**Example Entity: User.java**

```java
package com.nute.training.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    private String avatar;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Role {
        ADMIN, TEACHER, STUDENT
    }

    public enum Status {
        ACTIVE, INACTIVE, SUSPENDED
    }
}
```

**Example Entity: Course.java**

```java
package com.nute.training.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
@Data
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_type_id")
    private CourseType courseType;

    @Column(unique = true, nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_hours")
    private Integer durationHours;

    @Column(name = "duration_sessions")
    private Integer durationSessions;

    @Column(name = "tuition_fee", precision = 10, scale = 2)
    private BigDecimal tuitionFee;

    @Column(name = "max_students")
    private Integer maxStudents;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus status = CourseStatus.DRAFT;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum CourseStatus {
        DRAFT, ACTIVE, INACTIVE
    }
}
```

**Làm tương tự cho các Entity còn lại**

---

### Bước 5: Tạo Repository Interfaces

```java
package com.nute.training.repository;

import com.nute.training.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

```java
package com.nute.training.repository;

import com.nute.training.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByStatus(Course.CourseStatus status);
    Optional<Course> findByCode(String code);
}
```

**Tạo repository cho tất cả entities**

---

### ✅ Checklist Tuần 1-2

- [ ] Spring Boot project đã setup
- [ ] Database đã tạo và có seed data
- [ ] Tất cả 9 Entity classes đã tạo
- [ ] Tất cả Repository interfaces đã tạo
- [ ] Test kết nối database thành công
- [ ] Application chạy được (mvn spring-boot:run)

---

## 📅 TUẦN 3-4: AUTHENTICATION & ADMIN CRUD

### Bước 1: Cấu hình Spring Security

```java
package com.nute.training.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/login", "/register").permitAll()
                .requestMatchers("/admin/**").hasAuthority("ADMIN")
                .requestMatchers("/teacher/**").hasAuthority("TEACHER")
                .requestMatchers("/student/**").hasAuthority("STUDENT")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/access-denied")
            );

        return http.build();
    }
}
```

---

### Bước 2: Tạo UserDetailsService

```java
package com.nute.training.service;

import com.nute.training.entity.User;
import com.nute.training.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .authorities(Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name())))
            .build();
    }
}
```

---

### Bước 3: Tạo Controllers

**AuthController.java**

```java
package com.nute.training.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        // Redirect theo role
        return "redirect:/admin/dashboard";
    }
}
```

**AdminCourseController.java**

```java
package com.nute.training.controller.admin;

import com.nute.training.entity.Course;
import com.nute.training.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/courses")
@RequiredArgsConstructor
public class AdminCourseController {

    private final CourseService courseService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("courses", courseService.findAll());
        return "admin/courses/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("course", new Course());
        return "admin/courses/form";
    }

    @PostMapping
    public String create(@ModelAttribute Course course) {
        courseService.save(course);
        return "redirect:/admin/courses";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("course", courseService.findById(id));
        return "admin/courses/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Course course) {
        course.setId(id);
        courseService.save(course);
        return "redirect:/admin/courses";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        courseService.deleteById(id);
        return "redirect:/admin/courses";
    }
}
```

---

### Bước 4: Tạo Service Layer

```java
package com.nute.training.service;

import com.nute.training.entity.Course;
import com.nute.training.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    public Course findById(Long id) {
        return courseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Course not found"));
    }

    public Course save(Course course) {
        return courseRepository.save(course);
    }

    public void deleteById(Long id) {
        courseRepository.deleteById(id);
    }
}
```

---

### Bước 5: Tạo Thymeleaf Templates

**Cấu trúc templates:**

```
src/main/resources/templates/
├── layout/
│   └── main.html (base layout)
├── auth/
│   └── login.html
├── admin/
│   ├── dashboard.html
│   ├── courses/
│   │   ├── list.html
│   │   └── form.html
│   ├── classes/
│   └── users/
├── teacher/
└── student/
```

**login.html (Bootstrap 5)**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Đăng nhập</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
    <div class="container">
        <div class="row justify-content-center mt-5">
            <div class="col-md-4">
                <div class="card">
                    <div class="card-header text-center">
                        <h4>Đăng nhập</h4>
                    </div>
                    <div class="card-body">
                        <form th:action="@{/login}" method="post">
                            <div class="mb-3">
                                <label class="form-label">Tên đăng nhập</label>
                                <input type="text" name="username" class="form-control" required>
                            </div>
                            <div class="mb-3">
                                <label class="form-label">Mật khẩu</label>
                                <input type="password" name="password" class="form-control" required>
                            </div>
                            <button type="submit" class="btn btn-primary w-100">Đăng nhập</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
```

**admin/courses/list.html**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Quản lý khóa học</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <div class="container mt-4">
        <h2>Danh sách khóa học</h2>
        <a th:href="@{/admin/courses/create}" class="btn btn-primary mb-3">Tạo khóa học mới</a>

        <table class="table table-striped">
            <thead>
                <tr>
                    <th>Mã</th>
                    <th>Tên khóa học</th>
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
                    <td th:text="${course.durationHours} + ' giờ'"></td>
                    <td th:text="${#numbers.formatDecimal(course.tuitionFee, 0, 'COMMA', 0, 'POINT')} + ' VNĐ'"></td>
                    <td th:text="${course.status}"></td>
                    <td>
                        <a th:href="@{/admin/courses/{id}/edit(id=${course.id})}" class="btn btn-sm btn-warning">Sửa</a>
                        <form th:action="@{/admin/courses/{id}/delete(id=${course.id})}" method="post" style="display:inline;">
                            <button type="submit" class="btn btn-sm btn-danger" onclick="return confirm('Xác nhận xóa?')">Xóa</button>
                        </form>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</body>
</html>
```

---

### ✅ Checklist Tuần 3-4

- [ ] Spring Security đã cấu hình
- [ ] Đăng nhập thành công với admin/123456
- [ ] Admin CRUD khóa học hoàn thành
- [ ] Admin CRUD lớp học hoàn thành
- [ ] Admin CRUD người dùng hoàn thành
- [ ] Phân quyền hoạt động đúng

---

## 📅 TUẦN 5-6, 7-8, 9-10

*(Tiếp tục với Enrollment, Schedule, Attendance, Grading, Certificates, Reports)*

**Cấu trúc tương tự:**
1. Tạo Service
2. Tạo Controller
3. Tạo Thymeleaf template
4. Test chức năng

---

## 🎯 ĐIỂM QUAN TRỌNG

### Ưu tiên theo thứ tự:
1. ✅ **Chức năng hoạt động** (70%)
2. ✅ **UI đơn giản nhưng đủ dùng** (20%)
3. ✅ **Code sạch, có comment** (10%)

### Sử dụng template có sẵn:
- **AdminLTE** (https://adminlte.io/) - Free Bootstrap admin template
- Tiết kiệm thời gian thiết kế UI

### Git commit thường xuyên:
```bash
git add .
git commit -m "Implement course CRUD"
git push
```

---

**File tiếp theo:** Chi tiết implement từng module cụ thể nếu cần
