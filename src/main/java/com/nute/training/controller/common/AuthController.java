package com.nute.training.controller.common;

import com.nute.training.dto.RegisterDto;
import com.nute.training.entity.User;
import com.nute.training.service.UserService;
import com.nute.training.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller: AuthController
 * Xử lý đăng nhập, đăng xuất, đăng ký
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationHelper authenticationHelper;
    private final UserService userService;

    /**
     * Trang đăng nhập
     */
    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "expired", required = false) String expired,
            @RequestParam(value = "registerSuccess", required = false) String registerSuccess,
            Model model) {

        if (error != null) {
            model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng");
        }

        if (logout != null) {
            model.addAttribute("message", "Bạn đã đăng xuất thành công");
        }

        if (expired != null) {
            model.addAttribute("error", "Phiên làm việc đã hết hạn. Vui lòng đăng nhập lại");
        }

        if (registerSuccess != null) {
            model.addAttribute("message", "Đăng ký tài khoản thành công! Vui lòng đăng nhập.");
        }

        return "auth/login";
    }

    /**
     * Trang đăng ký
     */
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        return "auth/register";
    }

    /**
     * Xử lý đăng ký
     */
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("registerDto") RegisterDto registerDto, Model model) {
        try {
            User user = new User();
            user.setFullName(registerDto.getFullName());
            user.setEmail(registerDto.getEmail());
            user.setPhone(registerDto.getPhone());
            user.setUsername(registerDto.getUsername());
            user.setPassword(registerDto.getPassword());
            // Mặc định đăng ký là STUDENT nếu không có logic khác
            user.setRole(registerDto.getRole() != null ? User.Role.valueOf(registerDto.getRole()) : User.Role.STUDENT);

            userService.createUser(user);

            return "redirect:/login?registerSuccess";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        } catch (Exception e) {
            log.error("Error registering user", e);
            model.addAttribute("error", "Đã có lỗi xảy ra. Vui lòng thử lại sau.");
            return "auth/register";
        }
    }

    /**
     * Trang access denied
     */
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/403";
    }

    /**
     * Redirect đến dashboard theo role
     */
    @GetMapping("/dashboard")
    public String dashboard() {
        User currentUser = authenticationHelper.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("User {} accessing dashboard with role {}",
                currentUser.getUsername(), currentUser.getRole());

        return switch (currentUser.getRole()) {
            case ADMIN -> "redirect:/admin/dashboard";
            case TEACHER -> "redirect:/teacher/dashboard";
            case STUDENT -> "redirect:/student/dashboard";
        };
    }
}
