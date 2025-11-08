package com.example.backendspring.controller;

import com.example.backendspring.dto.calorie.DailyCalorieResponse;
import com.example.backendspring.dto.calorie.MonthlyCalorieResponse;
import com.example.backendspring.dto.common.ApiResponse;
import com.example.backendspring.service.CalorieService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/calories")
@RequiredArgsConstructor
public class CalorieController {
    
    private final CalorieService calorieService;
    
    /**
     * 일일 칼로리 조회
     * GET /api/calories/daily/{uniqueCode}/{date}
     * 
     * Android에서 PersonalCaloriePage에서 사용
     */
    @GetMapping("/daily/{uniqueCode}/{date}")
    public ResponseEntity<ApiResponse<DailyCalorieResponse>> getDailyCalories(
            @PathVariable String uniqueCode,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            DailyCalorieResponse response = calorieService.getDailyCalories(uniqueCode, date);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("일일 칼로리 조회 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 월별 칼로리 조회
     * GET /api/calories/monthly/{uniqueCode}/{year}/{month}
     * 
     * Android에서 CalendarPage에서 사용
     */
    @GetMapping("/monthly/{uniqueCode}/{year}/{month}")
    public ResponseEntity<ApiResponse<MonthlyCalorieResponse>> getMonthlyCalories(
            @PathVariable String uniqueCode,
            @PathVariable Integer year,
            @PathVariable Integer month) {
        try {
            MonthlyCalorieResponse response = calorieService.getMonthlyCalories(uniqueCode, year, month);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("월별 칼로리 조회 중 오류가 발생했습니다"));
        }
    }
}
