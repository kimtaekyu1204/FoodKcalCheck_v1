package com.example.android.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.android.data.UserSession
import com.example.android.data.api.RetrofitInstance
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarPage(navController: NavController) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val currentMonth = selectedDate.month
    val currentYear = selectedDate.year
    
    // API 데이터 상태
    var monthlyCalories by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var targetCalories by remember { mutableIntStateOf(UserSession.dailyCalorieGoal) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()

    // 월별 칼로리 데이터 로드
    LaunchedEffect(currentYear, currentMonth.value) {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitInstance.api.getMonthlyCalories(
                    uniqueCode = UserSession.uniqueCode,
                    year = currentYear,
                    month = currentMonth.value
                )
                
                if (response.success && response.data != null) {
                    monthlyCalories = response.data.dailyCalories
                    targetCalories = response.data.targetCalories
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = "네트워크 오류: ${e.message}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // 날짜 리스트 생성 (현재 달의 1일부터 마지막 날까지)
    val daysInMonth = currentMonth.length(currentYear % 4 == 0 && (currentYear % 100 != 0 || currentYear % 400 == 0))
    val firstDayOfWeek = LocalDate.of(currentYear, currentMonth, 1).dayOfWeek.value % 7

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp)
    ) {
        // 헤더
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${currentMonth.value}월 칼로리 리스트",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "목표: ${targetCalories}kcal",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        }
        
        // 에러 메시지 표시
        errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                )
            ) {
                Text(
                    text = error,
                    fontSize = 12.sp,
                    color = Color.Red,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 월 네비게이션
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                selectedDate = selectedDate.minusMonths(1)
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "이전 달",
                    tint = Color.Black
                )
            }

            Text(
                text = "${currentYear}년 ${currentMonth.value}월",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            IconButton(onClick = {
                selectedDate = selectedDate.plusMonths(1)
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "다음 달",
                    tint = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 요일 헤더
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("일", "월", "화", "수", "목", "금", "토").forEach { day ->
                Text(
                    text = day,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (day == "일") Color.Red else Color.Black,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 캘린더 그리드
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 총 셀 수 계산 (빈 칸 + 날짜)
            val totalCells = firstDayOfWeek + daysInMonth
            val weeks = (totalCells + 6) / 7 // 올림 계산

            // 주 단위로 Row 생성
            for (week in 0 until weeks) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 한 주의 7일
                    for (dayOfWeek in 0 until 7) {
                        val cellIndex = week * 7 + dayOfWeek

                        if (cellIndex < firstDayOfWeek || cellIndex >= firstDayOfWeek + daysInMonth) {
                            // 빈 칸
                            Spacer(modifier = Modifier.weight(1f))
                        } else {
                            // 날짜 표시
                            val dayNumber = cellIndex - firstDayOfWeek + 1
                            val date = LocalDate.of(currentYear, currentMonth, dayNumber)
                            val isSelected = date == selectedDate
                            
                            // API에서 가져온 실제 칼로리 데이터 확인
                            val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                            val actualCalories = monthlyCalories[dateString]
                            val hasData = actualCalories != null
                            
                            // 목표 대비 초과 여부
                            val isExceeded = actualCalories != null && actualCalories > targetCalories

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .background(
                                        color = when {
                                            !hasData -> Color.Transparent
                                            isExceeded -> Color(0xFFFFEBEE) // 초과 시 빨간색
                                            else -> Color(0xFFE8F5E9) // 정상 시 초록색
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { 
                                        selectedDate = date
                                        navController.navigate("personal_calorie/${dateString}")
                                     },
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = dayNumber.toString(),
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black
                                    )
                                    if (hasData && actualCalories != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "🍽️ $targetCalories",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "$actualCalories",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isExceeded) Color.Red else Color(0xFF4CAF50)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 주 간격
                if (week < weeks - 1) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 내 목표 설정하기 버튼
        OutlinedButton(
            onClick = { navController.navigate("goal_setting") },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "내 목표 설정하기",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 촬영 및 업로드 버튼
        Button(
            onClick = { navController.navigate("camera") },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.CameraAlt,
                contentDescription = "촬영",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "촬영및 업로드 하기",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}


