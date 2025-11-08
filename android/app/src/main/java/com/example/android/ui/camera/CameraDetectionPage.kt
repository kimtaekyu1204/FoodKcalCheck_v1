package com.example.android.ui.camera

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.android.data.UserSession
import com.example.android.data.api.RetrofitInstance
import com.example.android.data.model.MealRequest
import com.example.android.data.model.MealType
import com.example.android.data.model.RecognizedFoodItem
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraDetectionPage(
    imageUri: String,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    
    // API 데이터 상태
    var recognizedFoods by remember { mutableStateOf<List<RecognizedFoodItem>>(emptyList()) }
    var totalCalories by remember { mutableIntStateOf(0) }
    var foodCount by remember { mutableIntStateOf(0) }
    var food1Name by remember { mutableStateOf<String?>(null) }
    var food1Calories by remember { mutableStateOf<Int?>(null) }
    var food2Name by remember { mutableStateOf<String?>(null) }
    var food2Calories by remember { mutableStateOf<Int?>(null) }
    var food3Name by remember { mutableStateOf<String?>(null) }
    var food3Calories by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    
    // 음식 인식 API 호출
    LaunchedEffect(imageUri) {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                // URI를 File로 변환
                val uri = Uri.parse(imageUri)
                val file = File(uri.path ?: return@launch)
                
                // MultipartBody.Part 생성
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
                val uniqueCodeBody = UserSession.uniqueCode.toRequestBody("text/plain".toMediaTypeOrNull())
                
                // API 호출
                val response = RetrofitInstance.api.recognizeFood(body, uniqueCodeBody)
                
                if (response.success && response.data != null) {
                    val data = response.data
                    foodCount = data.foodCount
                    food1Name = data.food1Name
                    food1Calories = data.food1Calories
                    food2Name = data.food2Name
                    food2Calories = data.food2Calories
                    food3Name = data.food3Name
                    food3Calories = data.food3Calories
                    totalCalories = data.totalCalories
                    
                    // UI 표시용 리스트 변환
                    recognizedFoods = data.toFoodList()
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = "음식 인식 실패: ${e.message}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "음식 감지",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
            
            // 촬영된 이미지 표시
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    AsyncImage(
                        model = Uri.parse(imageUri),
                        contentDescription = "촬영된 음식 이미지",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
            
            // 로딩 인디케이터
            if (isLoading) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "AI가 음식을 분석 중입니다...",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
            
            // 에러 메시지
            errorMessage?.let { error ->
                item {
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
            }
            
            // 인식 결과 표시
            if (!isLoading && recognizedFoods.isNotEmpty()) {
                item {
                    Surface(
                        color = Color(0xFFE3F2FD),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(36.dp)
                            )
                            Column {
                                Text(
                                    text = "${foodCount}개의 음식이 감지되었습니다",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "총 ${totalCalories}kcal",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // 인식된 음식 목록
                item {
                    Text(
                        text = "인식된 음식",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
                
                items(recognizedFoods) { food ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = food.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "신뢰도: ${(food.confidence * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                text = "${food.calories} kcal",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
            
            // 버튼들
            if (!isLoading) {
                item {
                    // 다시 촬영하기 버튼
                    OutlinedButton(
                        onClick = { navController.navigate("camera") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Black
                        ),
                        enabled = !isSaving
                    ) {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = "다시 촬영",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "다시 촬영하기",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // 확인 및 저장 버튼
                item {
                    Button(
                        onClick = {
                            if (foodCount > 0) {
                                // 식사 추가 API 호출
                                scope.launch {
                                    isSaving = true
                                    try {
                                        val currentTime = LocalTime.now()
                                        val currentDate = LocalDate.now()
                                        
                                        // 현재 시간대에 따라 식사 타입 결정
                                        val mealType = when (currentTime.hour) {
                                            in 0..10 -> MealType.BREAKFAST.value
                                            in 11..15 -> MealType.LUNCH.value
                                            in 16..20 -> MealType.DINNER.value
                                            else -> MealType.SNACK.value
                                        }
                                        
                                        val mealRequest = MealRequest(
                                            userUniqueCode = UserSession.uniqueCode,
                                            mealDate = currentDate.toString(),
                                            mealTime = currentTime.toString(),
                                            mealType = mealType,
                                            foodCount = foodCount,
                                            food1Name = food1Name,
                                            food1Calories = food1Calories,
                                            food2Name = food2Name,
                                            food2Calories = food2Calories,
                                            food3Name = food3Name,
                                            food3Calories = food3Calories
                                        )
                                        
                                        val response = RetrofitInstance.api.createMeal(mealRequest)
                                        
                                        if (response.success) {
                                            // 성공 시 캘린더로 이동
                                            navController.navigate("calendar") {
                                                popUpTo("calendar") { inclusive = true }
                                            }
                                        } else {
                                            errorMessage = "식사 저장 실패: ${response.message}"
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "식사 저장 중 오류: ${e.message}"
                                        e.printStackTrace()
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            } else {
                                // 인식된 음식이 없으면 그냥 캘린더로 이동
                                navController.navigate("calendar")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "저장 중...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "확인",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (foodCount > 0) "확인 및 저장" else "캘린더로 돌아가기",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // 안내 텍스트
                if (foodCount > 0) {
                    item {
                        Text(
                            text = "확인 버튼을 누르면 식사가 자동으로 저장됩니다",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 20.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
