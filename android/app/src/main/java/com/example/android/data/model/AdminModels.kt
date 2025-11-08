package com.example.android.data.model

import com.google.gson.annotations.SerializedName

// 관리자 로그인 요청
data class AdminLoginRequest(
    val username: String,
    val password: String
)

// 관리자 로그인 응답
data class AdminLoginResponse(
    val adminId: Long,
    val username: String,
    val message: String
)

// 회원 관리 응답
data class UserManagementResponse(
    val userId: Long,
    val uniqueCode: String,
    val name: String,
    val email: String,
    val dailyCalorieGoal: Int,
    val createdAt: String
)

// 비밀번호 재설정 요청
data class ResetPasswordRequest(
    val newPassword: String
)
