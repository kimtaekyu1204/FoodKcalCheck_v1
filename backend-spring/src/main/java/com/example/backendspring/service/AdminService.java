package com.example.backendspring.service;

import com.example.backendspring.dto.admin.AdminLoginRequest;
import com.example.backendspring.dto.admin.AdminLoginResponse;
import com.example.backendspring.dto.admin.UserManagementResponse;
import com.example.backendspring.entity.Admin;
import com.example.backendspring.entity.User;
import com.example.backendspring.repository.AdminRepository;
import com.example.backendspring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 관리자 로그인
     */
    public AdminLoginResponse login(AdminLoginRequest request) {
        Admin admin = adminRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("관리자를 찾을 수 없습니다"));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        log.info("관리자 로그인 성공: {}", admin.getUsername());

        return AdminLoginResponse.builder()
                .adminId(admin.getId())
                .username(admin.getUsername())
                .message("로그인 성공")
                .build();
    }

    /**
     * 전체 회원 목록 조회
     */
    public List<UserManagementResponse> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user -> UserManagementResponse.builder()
                        .userId(user.getId())
                        .uniqueCode(user.getUniqueCode())
                        .name(user.getName())
                        .email(user.getEmail())
                        .dailyCalorieGoal(user.getDailyCalorieGoal())
                        .createdAt(user.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 회원 비밀번호 재설정
     */
    @Transactional
    public void resetUserPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("회원 비밀번호 재설정 완료: userId={}", userId);
    }

    /**
     * 회원 영구 삭제
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        userRepository.delete(user);

        log.info("회원 영구 삭제 완료: userId={}, uniqueCode={}", userId, user.getUniqueCode());
    }

    /**
     * 초기 관리자 계정 생성 (애플리케이션 시작 시 자동 생성)
     * 
     * 프로덕션 환경에서는 환경변수를 사용하여 관리자 계정을 설정하세요:
     * ADMIN_USERNAME, ADMIN_PASSWORD
     */
    @Transactional
    public void createDefaultAdminIfNotExists() {
        // 환경변수에서 관리자 정보 읽기 (없으면 기본값)
        String adminUsername = System.getenv().getOrDefault("ADMIN_USERNAME", "admin");
        String adminPassword = System.getenv().getOrDefault("ADMIN_PASSWORD", "admin1234");
        
        // 관리자 계정이 없으면 생성
        if (!adminRepository.existsByUsername(adminUsername)) {
            Admin admin = Admin.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .build();

            adminRepository.save(admin);
            log.info("✅ 관리자 계정 생성 완료: username={}", adminUsername);
        } else {
            log.info("ℹ️ 관리자 계정이 이미 존재합니다: username={}", adminUsername);
        }
    }
}
