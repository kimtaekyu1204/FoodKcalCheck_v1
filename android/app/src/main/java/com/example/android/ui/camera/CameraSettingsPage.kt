package com.example.android.ui.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraSettingsPage(
    onBackClick: () -> Unit = {}
) {
    var cameraQuality by remember { mutableStateOf("고화질") }
    var aiMode by remember { mutableStateOf("정밀") }
    var autoFlash by remember { mutableStateOf(true) }
    var gridLine by remember { mutableStateOf(true) }
    var detectionSound by remember { mutableStateOf(true) }
    var instantCalorieDisplay by remember { mutableStateOf(true) }
    var photoStorage by remember { mutableStateOf("앱 전용") }
    var autoDelete by remember { mutableStateOf(true) }
    var calorieDatabase by remember { mutableStateOf("한국음식") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // 카메라 화질
            SettingsItem(
                title = "카메라 화질",
                subtitle = "사진 품질 설정",
                value = cameraQuality,
                onClick = { /* 화질 선택 다이얼로그 */ }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // AI 인식 모드
            SettingsItem(
                title = "AI 인식 모드",
                subtitle = "음식 자동 감지 정확도",
                value = aiMode,
                onClick = { /* 모드 선택 다이얼로그 */ }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // 자동 플래시
            SwitchItem(
                title = "자동 플래시",
                subtitle = "어두운 환경에서 자동 활성화",
                checked = autoFlash,
                onCheckedChange = { autoFlash = it }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // 그리드 라인
            SwitchItem(
                title = "그리드 라인",
                subtitle = "촬영 가이드 표시",
                checked = gridLine,
                onCheckedChange = { gridLine = it }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // 음식 감지 소리
            SwitchItem(
                title = "음식 감지 소리",
                subtitle = "음식 인식 시 알림음",
                checked = detectionSound,
                onCheckedChange = { detectionSound = it }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // 즉시 칼로리 표시
            SwitchItem(
                title = "즉시 칼로리 표시",
                subtitle = "촬영 즉시 칼로리 정보 표시",
                checked = instantCalorieDisplay,
                onCheckedChange = { instantCalorieDisplay = it }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // 사진 저장 위치
            SettingsItem(
                title = "사진 저장 위치",
                subtitle = "촬영한 사진 보관 설정",
                value = photoStorage,
                onClick = { /* 저장 위치 선택 다이얼로그 */ }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // 자동 삭제
            SwitchItem(
                title = "자동 삭제",
                subtitle = "30일 후 자동으로 사진 삭제",
                checked = autoDelete,
                onCheckedChange = { autoDelete = it }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // 칼로리 데이터베이스
            SettingsItem(
                title = "칼로리 데이터베이스",
                subtitle = "음식 정보 정확도 향상",
                value = calorieDatabase,
                onClick = { /* 데이터베이스 선택 다이얼로그 */ }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 설정 초기화 버튼
            OutlinedButton(
                onClick = { /* 설정 초기화 */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "설정 초기화",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 버전 정보
            Text(
                text = "버전 1.2.0 • 최종 업데이트: 2025.10.26",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = value,
                fontSize = 14.sp,
                color = Color.Black
            )
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun SwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}



