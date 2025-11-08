package com.example.android.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.android.data.UserSession
import com.example.android.data.api.RetrofitInstance
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalSettingPage(navController: NavController) {
    var goalCalories by remember { mutableFloatStateOf(UserSession.dailyCalorieGoal.toFloat()) }
    var goalInput by remember { mutableStateOf(UserSession.dailyCalorieGoal.toString()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // 슬라이더 값 변경 시 입력 필드도 업데이트
    LaunchedEffect(goalCalories) {
        goalInput = goalCalories.toInt().toString()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "내 목표 설정하기",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(horizontal = 30.dp, vertical = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 안내 문구
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "💡 일일 목표 칼로리 설정",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "건강한 식습관을 위한 하루 목표 칼로리를 설정하세요.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 현재 목표 칼로리 표시
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "목표 칼로리",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = goalCalories.toInt().toString(),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Kcal",
                            fontSize = 24.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 슬라이더
            Text(
                text = "슬라이더로 조정",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(12.dp))

            Slider(
                value = goalCalories,
                onValueChange = { goalCalories = it },
                valueRange = 1000f..4000f,
                steps = 29, // 100kcal 단위 (1000~4000, 총 30개 구간)
                modifier = Modifier.fillMaxWidth()
            )

            // 범위 표시
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "1000 kcal",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "4000 kcal",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 직접 입력
            Text(
                text = "직접 입력",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = goalInput,
                onValueChange = {
                    goalInput = it
                    it.toIntOrNull()?.let { value ->
                        if (value in 1000..4000) {
                            goalCalories = value.toFloat()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("예: 2000") },
                suffix = { Text("kcal") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 추천 칼로리
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RecommendedCalorieButton("1500", onClick = {
                    goalCalories = 1500f
                    goalInput = "1500"
                })
                RecommendedCalorieButton("2000", onClick = {
                    goalCalories = 2000f
                    goalInput = "2000"
                })
                RecommendedCalorieButton("2500", onClick = {
                    goalCalories = 2500f
                    goalInput = "2500"
                })
            }

            // 에러/성공 메시지
            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    )
                ) {
                    Text(
                        text = error,
                        fontSize = 14.sp,
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            successMessage?.let { success ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9)
                    )
                ) {
                    Text(
                        text = success,
                        fontSize = 14.sp,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 저장 버튼
            Button(
                onClick = {
                    val finalGoal = goalInput.toIntOrNull()
                    when {
                        finalGoal == null -> {
                            errorMessage = "올바른 칼로리 값을 입력하세요"
                        }
                        finalGoal < 1000 -> {
                            errorMessage = "최소 1000 kcal 이상 설정해주세요"
                        }
                        finalGoal > 4000 -> {
                            errorMessage = "최대 4000 kcal 이하로 설정해주세요"
                        }
                        else -> {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                successMessage = null
                                try {
                                    // API 호출하여 서버에 저장
                                    val response = RetrofitInstance.api.updateDailyCalorieGoal(
                                        uniqueCode = UserSession.uniqueCode,
                                        dailyCalorieGoal = finalGoal
                                    )
                                    
                                    if (response.success) {
                                        // UserSession 업데이트
                                        UserSession.dailyCalorieGoal = finalGoal
                                        
                                        successMessage = "목표 칼로리가 ${finalGoal}kcal로 설정되었습니다"
                                        
                                        // 2초 후 뒤로가기
                                        kotlinx.coroutines.delay(2000)
                                        navController.popBackStack()
                                    } else {
                                        errorMessage = response.message ?: "저장 실패"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "저장 중 오류 발생: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "저장하기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun RowScope.RecommendedCalorieButton(
    calorie: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .height(40.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = calorie,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

