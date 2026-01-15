package com.nute.training.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web Configuration: WebConfig
 * Cấu hình Interceptors và các thiết lập Web MVC khác
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final StudentProfileInterceptor studentProfileInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Áp dụng kiểm tra hồ sơ cho các đường dẫn của sinh viên
        registry.addInterceptor(studentProfileInterceptor)
                .addPathPatterns("/student/**")
                .excludePathPatterns("/css/**", "/js/**", "/assets/**", "/webjars/**");
    }
}
