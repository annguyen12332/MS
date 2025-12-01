package com.nute.training.dto;

import lombok.Data;

@Data
public class RegisterDto {
    private String fullName;
    private String email;
    private String phone;
    private String username;
    private String password;
    private String role;
}
