package com.example.android.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.android.data.api.RetrofitInstance
import com.example.android.data.model.ResetPasswordRequest
import com.example.android.data.model.UserManagementResponse
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementPage(navController: NavController) {
    var users by remember { mutableStateOf<List<UserManagementResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showResetPasswordDialog by remember { mutableStateOf<UserManagementResponse?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<UserManagementResponse?>(null) }

    val scope = rememberCoroutineScope()

    // 사용자 목록 로드
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = RetrofitInstance.api.getAllUsers()
                if (response.success && response.data != null) {
                    users = response.data
                } else {
                    errorMessage = response.message ?: "사용자 목록을 불러올 수 없습니다"
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
                title = { Text("회원 관리") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "뒤로가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color.Red,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            navController.popBackStack()
                        }) {
                            Text("뒤로가기")
                        }
                    }
                }
                users.isEmpty() -> {
                    Text(
                        text = "등록된 회원이 없습니다",
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(users) { user ->
                            UserCard(
                                user = user,
                                onResetPassword = { showResetPasswordDialog = user },
                                onDeleteUser = { showDeleteConfirmDialog = user }
                            )
                        }
                    }
                }
            }
        }
    }

    // 비밀번호 재설정 다이얼로그
    showResetPasswordDialog?.let { user ->
        ResetPasswordDialog(
            user = user,
            onDismiss = { showResetPasswordDialog = null },
            onConfirm = { newPassword ->
                scope.launch {
                    try {
                        val response = RetrofitInstance.api.resetUserPassword(
                            userId = user.userId,
                            request = ResetPasswordRequest(newPassword)
                        )
                        if (response.success) {
                            errorMessage = "비밀번호가 재설정되었습니다"
                            showResetPasswordDialog = null
                        } else {
                            errorMessage = response.message ?: "비밀번호 재설정 실패"
                        }
                    } catch (e: Exception) {
                        errorMessage = "네트워크 오류: ${e.message}"
                        e.printStackTrace()
                    }
                }
            }
        )
    }

    // 회원 삭제 확인 다이얼로그
    showDeleteConfirmDialog?.let { user ->
        DeleteConfirmDialog(
            user = user,
            onDismiss = { showDeleteConfirmDialog = null },
            onConfirm = {
                scope.launch {
                    try {
                        val response = RetrofitInstance.api.deleteUser(user.userId)
                        if (response.success) {
                            users = users.filter { it.userId != user.userId }
                            errorMessage = "회원이 삭제되었습니다"
                            showDeleteConfirmDialog = null
                        } else {
                            errorMessage = response.message ?: "회원 삭제 실패"
                        }
                    } catch (e: Exception) {
                        errorMessage = "네트워크 오류: ${e.message}"
                        e.printStackTrace()
                    }
                }
            }
        )
    }
}

@Composable
fun UserCard(
    user: UserManagementResponse,
    onResetPassword: () -> Unit,
    onDeleteUser: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 회원 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "코드: ${user.uniqueCode}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "이메일: ${user.email}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "목표: ${user.dailyCalorieGoal} kcal",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "가입일: ${user.createdAt}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 관리 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 비밀번호 재설정
                OutlinedButton(
                    onClick = onResetPassword,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("비밀번호 재설정", fontSize = 13.sp)
                }

                // 회원 삭제
                Button(
                    onClick = onDeleteUser,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("회원 삭제", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun ResetPasswordDialog(
    user: UserManagementResponse,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("비밀번호 재설정") },
        text = {
            Column {
                Text(
                    text = "${user.name} (${user.uniqueCode})",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        errorMessage = null
                    },
                    label = { Text("새 비밀번호") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errorMessage = null
                    },
                    label = { Text("비밀번호 확인") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        newPassword.isBlank() -> {
                            errorMessage = "비밀번호를 입력하세요"
                        }
                        newPassword.length < 6 -> {
                            errorMessage = "비밀번호는 최소 6자 이상이어야 합니다"
                        }
                        newPassword != confirmPassword -> {
                            errorMessage = "비밀번호가 일치하지 않습니다"
                        }
                        else -> {
                            onConfirm(newPassword)
                        }
                    }
                }
            ) {
                Text("재설정")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@Composable
fun DeleteConfirmDialog(
    user: UserManagementResponse,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("회원 삭제 확인") },
        text = {
            Column {
                Text("정말로 이 회원을 삭제하시겠습니까?")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "이름: ${user.name}",
                    fontWeight = FontWeight.Bold
                )
                Text(text = "코드: ${user.uniqueCode}")
                Text(text = "이메일: ${user.email}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "이 작업은 되돌릴 수 없습니다.",
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {
                Text("삭제")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
