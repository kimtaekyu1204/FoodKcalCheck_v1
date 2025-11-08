package com.example.android.ui.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.android.data.api.RetrofitInstance
import com.example.android.data.model.FoodRequest
import com.example.android.data.model.MealRequest
import com.example.android.data.UserSession
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualInputPage(navController: NavController) {
    var foodName by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var showSearchDialog by remember { mutableStateOf(false) }
    var searchResult by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("수동 입력") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "뒤로가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
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
                        text = "💡 음식 정보 입력",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "음식 이름을 입력하면 데이터베이스에서 자동으로 칼로리를 찾아줍니다.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 음식 이름 입력
            Text(
                text = "음식 이름",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = foodName,
                onValueChange = {
                    foodName = it
                    searchResult = null
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("예: 쌀밥, 김치찌개") },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (foodName.isNotBlank()) {
                                scope.launch {
                                    isLoading = true
                                    try {
                                        // FastAPI에서 음식 칼로리 조회
                                        val response = RetrofitInstance.api.searchFoodCalories(foodName)
                                        if (response.success && response.data != null) {
                                            searchResult = Pair(response.data.foodName, response.data.calories)
                                            showSearchDialog = true
                                        } else {
                                            errorMessage = "음식을 찾을 수 없습니다"
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "검색 중 오류 발생: ${e.message}"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        },
                        enabled = foodName.isNotBlank() && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Search, "검색")
                        }
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 칼로리 입력
            Text(
                text = "칼로리 (kcal)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = calories,
                onValueChange = { calories = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("예: 334") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 에러 메시지
            errorMessage?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 추가 버튼
            Button(
                onClick = {
                    scope.launch {
                        when {
                            foodName.isBlank() -> {
                                errorMessage = "음식 이름을 입력하세요"
                            }
                            calories.isBlank() -> {
                                errorMessage = "칼로리를 입력하세요"
                            }
                            calories.toIntOrNull() == null -> {
                                errorMessage = "올바른 칼로리 값을 입력하세요"
                            }
                            else -> {
                                isLoading = true
                                errorMessage = null
                                try {
                                    // 현재 시간 기준으로 식사 타입 결정
                                    val mealType = when (LocalTime.now().hour) {
                                        in 0..10 -> "BREAKFAST"
                                        in 11..15 -> "LUNCH"
                                        in 16..19 -> "SNACK"
                                        else -> "DINNER"
                                    }

                                    // 식사 추가 API 호출
                                    val mealRequest = MealRequest(
                                        userUniqueCode = UserSession.uniqueCode,
                                        mealType = mealType,
                                        mealDate = LocalDate.now().toString(),
                                        mealTime = LocalTime.now().toString(),
                                        foodCount = 1,
                                        food1Name = foodName,
                                        food1Calories = calories.toInt()
                                    )

                                    val response = RetrofitInstance.api.createMeal(mealRequest)

                                    if (response.success) {
                                        // 성공 시 캘린더 페이지로 이동
                                        navController.navigate("calendar") {
                                            popUpTo("camera") { inclusive = true }
                                        }
                                    } else {
                                        errorMessage = response.message ?: "추가 실패"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "오류 발생: ${e.message}"
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
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "추가하기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // 검색 결과 팝업
    if (showSearchDialog && searchResult != null) {
        AlertDialog(
            onDismissRequest = { showSearchDialog = false },
            title = { Text("음식 정보") },
            text = {
                Column {
                    Text(
                        text = searchResult!!.first,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${searchResult!!.second} kcal",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "이 정보를 사용하시겠습니까?",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        calories = searchResult!!.second.toString()
                        showSearchDialog = false
                        errorMessage = null
                    }
                ) {
                    Text("사용하기")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSearchDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}
