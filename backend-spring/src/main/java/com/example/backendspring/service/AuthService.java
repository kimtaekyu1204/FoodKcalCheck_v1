package com.example.backendspring.service;

import com.example.backendspring.dto.auth.AuthResponse;
import com.example.backendspring.dto.auth.LoginRequest;
import com.example.backendspring.dto.auth.SignUpRequest;
import com.example.backendspring.entity.User;
import com.example.backendspring.repository.UserRepository;
import com.example.backendspring.util.UniqueCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Transactional
    public AuthResponse signUp(SignUpRequest request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다");
        }
        
        // 유저 고유 코드 생성 (중복 체크)
        String uniqueCode = generateUniqueCode();
        
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        
        // 사용자 생성
        User user = User.builder()
                .uniqueCode(uniqueCode)
                .name(request.getName())
                .email(request.getEmail())
                .password(encodedPassword)
                .dailyCalorieGoal(request.getDailyCalorieGoal() != null ? 
                        request.getDailyCalorieGoal() : 2000) // 기본값 2000kcal
                .build();
        
        User savedUser = userRepository.save(user);
        
        return AuthResponse.builder()
                .userId(savedUser.getId())
                .uniqueCode(savedUser.getUniqueCode())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .dailyCalorieGoal(savedUser.getDailyCalorieGoal())
                .message("회원가입이 완료되었습니다")
                .build();
    }
    
    public AuthResponse login(LoginRequest request) {
        // 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다"));
        
        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다");
        }
        
        return AuthResponse.builder()
                .userId(user.getId())
                .uniqueCode(user.getUniqueCode())
                .name(user.getName())
                .email(user.getEmail())
                .dailyCalorieGoal(user.getDailyCalorieGoal())
                .message("로그인에 성공했습니다")
                .build();
    }
    
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
    }
    
    public User getUserByUniqueCode(String uniqueCode) {
        return userRepository.findByUniqueCode(uniqueCode)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
    }
    
    @Transactional
    public void updateDailyCalorieGoal(String uniqueCode, Integer dailyCalorieGoal) {
        User user = getUserByUniqueCode(uniqueCode);
        user.setDailyCalorieGoal(dailyCalorieGoal);
        userRepository.save(user);
    }
    
    /**
     * 중복되지 않는 유저 고유 코드 생성
     */
    private String generateUniqueCode() {
        String uniqueCode;
        int attempts = 0;
        int maxAttempts = 10;
        
        do {
            uniqueCode = UniqueCodeGenerator.generate();
            attempts++;
            
            if (attempts > maxAttempts) {
                throw new RuntimeException("유저 고유 코드 생성 실패 (최대 시도 횟수 초과)");
            }
        } while (userRepository.existsByUniqueCode(uniqueCode));
        
        return uniqueCode;
    }
}
