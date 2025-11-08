package com.example.backendspring.controller;

import com.example.backendspring.dto.admin.AdminLoginRequest;
import com.example.backendspring.dto.admin.AdminLoginResponse;
import com.example.backendspring.dto.admin.ResetPasswordRequest;
import com.example.backendspring.dto.admin.UserManagementResponse;
import com.example.backendspring.dto.common.ApiResponse;
import com.example.backendspring.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * 관리자 로그인
     * POST /api/admin/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AdminLoginResponse>> login(@Valid @RequestBody AdminLoginRequest request) {
        try {
            AdminLoginResponse response = adminService.login(request);
            return ResponseEntity.ok(ApiResponse.success("로그인 성공", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("로그인 중 오류가 발생했습니다"));
        }
    }

    /**
     * 전체 회원 목록 조회
     * GET /api/admin/users
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserManagementResponse>>> getAllUsers() {
        try {
            List<UserManagementResponse> users = adminService.getAllUsers();
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("회원 목록 조회 중 오류가 발생했습니다"));
        }
    }

    /**
     * 회원 비밀번호 재설정
     * PUT /api/admin/users/{userId}/reset-password
     */
    @PutMapping("/users/{userId}/reset-password")
    public ResponseEntity<ApiResponse<String>> resetUserPassword(
            @PathVariable Long userId,
            @Valid @RequestBody ResetPasswordRequest request) {
        try {
            adminService.resetUserPassword(userId, request.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success("비밀번호가 재설정되었습니다", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("비밀번호 재설정 중 오류가 발생했습니다"));
        }
    }

    /**
     * 회원 영구 삭제
     * DELETE /api/admin/users/{userId}
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long userId) {
        try {
            adminService.deleteUser(userId);
            return ResponseEntity.ok(ApiResponse.success("회원이 영구 삭제되었습니다", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("회원 삭제 중 오류가 발생했습니다"));
        }
    }
}
