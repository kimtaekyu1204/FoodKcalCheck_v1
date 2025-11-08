package com.example.backendspring.repository;

import com.example.backendspring.entity.TrainingDataLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrainingDataLogRepository extends JpaRepository<TrainingDataLog, Long> {

    // 특정 유저의 학습 데이터 조회
    List<TrainingDataLog> findByUserUniqueCode(String userUniqueCode);

    // 특정 Meal에 연결된 학습 데이터 조회
    List<TrainingDataLog> findByMealId(Long mealId);

    // 특정 기간의 학습 데이터 조회
    List<TrainingDataLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // 최근 N개의 학습 데이터 조회 (관리자용)
    List<TrainingDataLog> findTop100ByOrderByCreatedAtDesc();
}
