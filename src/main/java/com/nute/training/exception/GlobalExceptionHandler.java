package com.nute.training.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global Exception Handler
 * Xử lý các ngoại lệ chung trong ứng dụng
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Xử lý lỗi 403 Access Denied
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access Denied: {}", ex.getMessage());
        ModelAndView modelAndView = new ModelAndView("error/403");
        modelAndView.addObject("errorMessage", "Bạn không có quyền truy cập vào tài nguyên này.");
        return modelAndView;
    }

    /**
     * Xử lý lỗi 404 Not Found (Khi request không khớp với bất kỳ handler nào)
     * Yêu cầu spring.mvc.throw-exception-if-no-handler-found=true và spring.web.resources.add-mappings=false
     * trong application.properties
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNotFoundException(NoHandlerFoundException ex) {
        log.warn("404 Not Found: {}", ex.getRequestURL());
        ModelAndView modelAndView = new ModelAndView("error/404");
        modelAndView.addObject("errorMessage", "Trang bạn tìm kiếm không tồn tại.");
        return modelAndView;
    }

    /**
     * Xử lý lỗi UnauthorizedException (Chưa đăng nhập)
     */
    @ExceptionHandler(UnauthorizedException.class)
    public String handleUnauthorizedException(UnauthorizedException ex, RedirectAttributes redirectAttributes) {
        log.warn("Unauthorized: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/login";
    }

    /**
     * Xử lý lỗi ResourceNotFoundException (Không tìm thấy tài nguyên)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource Not Found: {} - URL: {}", ex.getMessage(), request.getRequestURI());
        ModelAndView modelAndView = new ModelAndView("error/404");
        modelAndView.addObject("errorMessage", ex.getMessage());
        return modelAndView;
    }

    /**
     * Xử lý lỗi BusinessException (Vi phạm business rules)
     */
    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException ex, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        log.warn("Business Rule Violation: {} - URL: {}", ex.getMessage(), request.getRequestURI());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());

        // Redirect về trang trước đó nếu có, nếu không thì về dashboard
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }
        return "redirect:/dashboard";
    }

    /**
     * Xử lý lỗi chung (RuntimeException và các lỗi khác)
     * Không xử lý NoResourceFoundException để cho phép Spring Boot serve static resources bình thường
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneralException(Exception ex) {
        // Bỏ qua NoResourceFoundException để Spring Boot xử lý static resources
        if (ex instanceof NoResourceFoundException) {
            return null; // Return null để Spring Boot tiếp tục xử lý
        }

        log.error("Internal Server Error: {}", ex.getMessage(), ex);
        ModelAndView modelAndView = new ModelAndView("error/500");
        modelAndView.addObject("errorMessage", "Đã có lỗi xảy ra trong quá trình xử lý yêu cầu. Vui lòng thử lại sau.");
        modelAndView.addObject("detailedError", ex.getMessage());
        return modelAndView;
    }
}
