package com.example.android.data.model

// 로그인/회원가입 요청/응답

data class LoginRequest(
    val email: String,
    val password: String
)

data class SignUpRequest(
    val name: String,
    val email: String,
    val password: String,
    val dailyCalorieGoal: Int? = null
)

data class AuthResponse(
    val userId: Long,
    val uniqueCode: String,
    val name: String,
    val email: String,
    val dailyCalorieGoal: Int,
    val message: String
)
