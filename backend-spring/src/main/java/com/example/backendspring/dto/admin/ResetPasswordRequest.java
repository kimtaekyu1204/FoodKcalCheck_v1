package com.example.backendspring.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "새 비밀번호를 입력하세요")
    private String newPassword;
}
