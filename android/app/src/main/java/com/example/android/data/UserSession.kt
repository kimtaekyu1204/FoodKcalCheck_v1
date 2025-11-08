package com.example.android.data

/**
 * 임시 사용자 세션 관리
 * TODO: SharedPreferences 또는 DataStore로 개선 필요
 */
object UserSession {
    // 현재 로그인된 사용자 정보
    // 로그인 후 updateUserInfo()로 업데이트됩니다
    var userId: Long = 0L
    var uniqueCode: String = ""
    var userName: String = ""
    var userEmail: String = ""
    var dailyCalorieGoal: Int = 2000
    
    fun updateUserInfo(userId: Long, uniqueCode: String, name: String, email: String, goal: Int) {
        this.userId = userId
        this.uniqueCode = uniqueCode
        this.userName = name
        this.userEmail = email
        this.dailyCalorieGoal = goal
    }
    
    fun clear() {
        userId = 0L
        uniqueCode = ""
        userName = ""
        userEmail = ""
        dailyCalorieGoal = 2000
    }
}

