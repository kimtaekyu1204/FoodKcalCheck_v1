package com.example.android.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyDetailPage(
    date: String = "12월 10일",
    navController: NavController? = null,
    onBackClick: () -> Unit = {}
) {
    val targetCalories = 1500
    val actualCalories = 1650
    val exceededCalories = actualCalories - targetCalories

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = date,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { navController?.navigate("goal_setting") }) {
                        Text(
                            text = "내 목표\n설정하기",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(horizontal = 30.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 요약 카드
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // 목표 칼로리
                            Column {
                                Text(
                                    text = "목표 칼로리",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "${targetCalories.toString().replace(",", "")}",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "Kcal",
                                        fontSize = 16.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                            }

                            // 실제 섭취
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "실제 섭취",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "${actualCalories.toString().replace(",", "")}",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "Kcal",
                                        fontSize = 16.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 진행 바
                        LinearProgressIndicator(
                            progress = { (actualCalories.toFloat() / targetCalories.toFloat()).coerceAtMost(1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = if (exceededCalories > 0) Color(0xFFFF6B6B) else MaterialTheme.colorScheme.primary,
                            trackColor = Color(0xFFE0E0E0)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 경고 메시지
                        if (exceededCalories > 0) {
                            Text(
                                text = "⚠️ 목표보다 ${exceededCalories}Kcal 초과했어요",
                                fontSize = 14.sp,
                                color = Color(0xFFFF6B6B)
                            )
                        }
                    }
                }
            }

            // 식사 내역 제목
            item {
                Text(
                    text = "식사 내역",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            // 아침 식사 카드
            item {
                MealCard(
                    mealType = "아침",
                    time = "08:30",
                    totalCalories = 500,
                    items = listOf(
                        MealItem("차 (녹차)", 50),
                        MealItem("통밀 토스트", 250),
                        MealItem("삶은 계란", 200)
                    )
                )
            }

            // 점심 식사 카드
            item {
                MealCard(
                    mealType = "점심",
                    time = "12:45",
                    totalCalories = 300,
                    items = listOf(
                        MealItem("그린 샐러드", 50),
                        MealItem("닭가슴살 샐러드", 250)
                    )
                )
            }

            // 저녁 식사 카드
            item {
                MealCard(
                    mealType = "저녁",
                    time = "19:20",
                    totalCalories = 850,
                    items = listOf(
                        MealItem("쌀밥 (1공기)", 200),
                        MealItem("페퍼로니 피자 (3조각)", 600),
                        MealItem("콜라 (캔)", 50)
                    )
                )
            }

            // 식사 추가하기 버튼
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "식사 추가하기",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { /* 식사 추가 */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(85.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "추가",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // 촬영 및 업로드 버튼
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /* 촬영 및 업로드 */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "촬영",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "촬영 및 업로드 하기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
