package com.example.backendspring.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementResponse {
    private Long userId;
    private String uniqueCode;
    private String name;
    private String email;
    private Integer dailyCalorieGoal;
    private LocalDateTime createdAt;
}
