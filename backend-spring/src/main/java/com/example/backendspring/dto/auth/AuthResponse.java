package com.example.backendspring.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private Long userId;
    private String uniqueCode;  // 유저 고유 코드
    private String name;
    private String email;
    private Integer dailyCalorieGoal;
    private String message;
}

