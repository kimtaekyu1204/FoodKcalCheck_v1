package com.example.backendspring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "training_data_log", indexes = {
    @Index(name = "idx_user_code", columnList = "user_unique_code"),
    @Index(name = "idx_meal_id", columnList = "meal_id"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingDataLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "user_unique_code", nullable = false, length = 10)
    private String userUniqueCode;

    @Column(name = "meal_id")
    private Long mealId;

    @Column(name = "image_path", nullable = false, length = 512)
    private String imagePath;

    @Column(name = "ai_prediction", columnDefinition = "JSON")
    private String aiPrediction; // JSON 문자열로 저장

    @Column(name = "user_corrected_json", columnDefinition = "JSON")
    private String userCorrectedJson; // JSON 문자열로 저장

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
