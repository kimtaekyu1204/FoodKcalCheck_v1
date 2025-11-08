package com.example.backendspring.controller;

import com.example.backendspring.dto.auth.AuthResponse;
import com.example.backendspring.dto.auth.LoginRequest;
import com.example.backendspring.dto.auth.SignUpRequest;
import com.example.backendspring.dto.common.ApiResponse;
import com.example.backendspring.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * 회원가입
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
        try {
            AuthResponse response = authService.signUp(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("회원가입이 완료되었습니다", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("회원가입 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 로그인
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success("로그인에 성공했습니다", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("로그인 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 목표 칼로리 설정
     * PUT /api/auth/users/{uniqueCode}/goal
     */
    @PutMapping("/users/{uniqueCode}/goal")
    public ResponseEntity<ApiResponse<String>> updateDailyCalorieGoal(
            @PathVariable String uniqueCode,
            @RequestParam Integer dailyCalorieGoal) {
        try {
            authService.updateDailyCalorieGoal(uniqueCode, dailyCalorieGoal);
            return ResponseEntity.ok(ApiResponse.success("목표 칼로리가 업데이트되었습니다", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("목표 칼로리 업데이트 중 오류가 발생했습니다"));
        }
    }
}
