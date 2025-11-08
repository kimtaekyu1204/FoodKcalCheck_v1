package com.example.backendspring.dto.meal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodResponse {
    
    private Long id;
    private String name;
    private Integer calories;
    private String imageUrl;
}

