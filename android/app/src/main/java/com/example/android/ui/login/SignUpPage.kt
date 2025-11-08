package com.example.android.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.android.data.UserSession
import com.example.android.data.api.RetrofitInstance
import com.example.android.data.model.SignUpRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpPage(
    navController: NavController? = null,
    onBackClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var allAgree by remember { mutableStateOf(false) }
    var serviceAgree by remember { mutableStateOf(false) }
    var privacyAgree by remember { mutableStateOf(false) }
    var marketingAgree by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    // 회원가입 처리 함수
    fun handleSignUp() {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            errorMessage = "모든 필드를 입력해주세요"
            return
        }
        
        if (password != confirmPassword) {
            errorMessage = "비밀번호가 일치하지 않습니다"
            return
        }
        
        if (password.length < 8) {
            errorMessage = "비밀번호는 8자 이상이어야 합니다"
            return
        }
        
        if (!serviceAgree || !privacyAgree) {
            errorMessage = "필수 약관에 동의해주세요"
            return
        }
        
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val request = SignUpRequest(
                    name = name,
                    email = email,
                    password = password,
                    dailyCalorieGoal = 2000 // 기본값
                )
                val response = RetrofitInstance.api.signUp(request)
                
                if (response.success && response.data != null) {
                    // UserSession 업데이트
                    UserSession.updateUserInfo(
                        userId = response.data.userId,
                        uniqueCode = response.data.uniqueCode,
                        name = response.data.name,
                        email = response.data.email,
                        goal = response.data.dailyCalorieGoal
                    )
                    // 캘린더로 이동
                    navController?.navigate("calendar") {
                        popUpTo("signup") { inclusive = true }
                    } ?: onSignUpClick()
                } else {
                    errorMessage = response.message ?: "회원가입에 실패했습니다"
                }
            } catch (e: Exception) {
                errorMessage = "네트워크 오류: ${e.message}"
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
                        text = "회원가입",
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
                .padding(horizontal = 30.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 환영 메시지
            Column {
                Text(
                    text = "환영합니다!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "CaloriTrack과 함께 건강한 식습관을 시작하세요",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 이름 입력
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "이름",
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = {
                        Text(
                            text = "이름을 입력하세요",
                            color = Color.Gray
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 이메일 입력
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "이메일",
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = {
                        Text(
                            text = "이메일을 입력하세요",
                            color = Color.Gray
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 비밀번호 입력
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "비밀번호",
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = {
                        Text(
                            text = "비밀번호를 입력하세요 (8자 이상)",
                            color = Color.Gray
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "비밀번호 숨기기" else "비밀번호 보기",
                                tint = Color.Gray
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // 비밀번호 강도 표시
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LinearProgressIndicator(
                        progress = { 
                            when {
                                password.length < 4 -> 0.33f
                                password.length < 8 -> 0.66f
                                else -> 1f
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp),
                        color = when {
                            password.length < 4 -> Color.Red
                            password.length < 8 -> Color(0xFFFFA726)
                            else -> Color(0xFF4CAF50)
                        },
                        trackColor = Color(0xFFE0E0E0)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "약함",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 비밀번호 확인
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "비밀번호 확인",
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = {
                        Text(
                            text = "비밀번호를 다시 입력하세요",
                            color = Color.Gray
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "비밀번호 숨기기" else "비밀번호 보기",
                                tint = Color.Gray
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 모두 동의하기
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = allAgree,
                    onCheckedChange = {
                        allAgree = it
                        serviceAgree = it
                        privacyAgree = it
                        marketingAgree = it
                    }
                )
                Text(
                    text = "모두 동의합니다",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 개별 동의 항목
            Column(
                modifier = Modifier.padding(start = 50.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AgreementItem(
                    text = "(필수) 서비스 이용약관 동의",
                    checked = serviceAgree,
                    onCheckedChange = { serviceAgree = it },
                    isRequired = true
                )
                AgreementItem(
                    text = "(필수) 개인정보 처리방침 동의",
                    checked = privacyAgree,
                    onCheckedChange = { privacyAgree = it },
                    isRequired = true
                )
                AgreementItem(
                    text = "(선택) 마케팅 정보 수신 동의",
                    checked = marketingAgree,
                    onCheckedChange = { marketingAgree = it },
                    isRequired = false
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // 에러 메시지 표시
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
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

            // 가입하기 버튼
            Button(
                onClick = { handleSignUp() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = name.isNotEmpty() && email.isNotEmpty() && 
                         password.isNotEmpty() && confirmPassword.isNotEmpty() &&
                         serviceAgree && privacyAgree && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "가입하기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 구분선
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color.Gray
                )
                Text(
                    text = "SNS로 간편가입",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SNS 로그인 버튼들 (간단히 표시)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // SNS 로그인 아이콘 버튼들
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 로그인 링크
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "이미 계정이 있으신가요?",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = onLoginClick) {
                    Text(
                        text = "로그인",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun AgreementItem(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isRequired: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
            Text(
                text = text,
                fontSize = 12.sp,
                color = Color.Black
            )
        }
        IconButton(onClick = { /* 약관 보기 */ }) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "약관 보기",
                modifier = Modifier.size(16.dp),
                tint = Color.Gray
            )
        }
    }
}



