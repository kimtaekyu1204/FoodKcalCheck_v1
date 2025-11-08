package com.example.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.android.ui.admin.AdminLoginPage
import com.example.android.ui.admin.AdminDashboardPage
import com.example.android.ui.admin.UserManagementPage
import com.example.android.ui.calendar.CalendarPage
import com.example.android.ui.calendar.PersonalCaloriePage
import com.example.android.ui.calendar.GoalSettingPage
import com.example.android.ui.camera.CameraPage
import com.example.android.ui.camera.CameraPermissionPage
import com.example.android.ui.camera.CameraDetectionPage
import com.example.android.ui.camera.CameraSettingsPage
import com.example.android.ui.camera.ManualInputPage
import com.example.android.ui.login.LoginPage
import com.example.android.ui.login.SignUpPage
import com.example.android.ui.theme.AndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "login") {
                        // 로그인 관련 페이지
                        composable("login") {
                            LoginPage(
                                navController = navController,
                                onSignUpClick = { navController.navigate("signup") },
                                onLoginClick = { navController.navigate("calendar") }
                            )
                        }
                        composable("signup") {
                            SignUpPage(
                                navController = navController,
                                onBackClick = { navController.popBackStack() },
                                onSignUpClick = { navController.navigate("calendar") },
                                onLoginClick = { navController.navigate("login") }
                            )
                        }

                        // 카메라 관련 페이지
                        composable("camera") {
                            CameraPage(navController = navController)
                        }
                        composable("camera_permission") {
                            CameraPermissionPage(
                                onBackClick = { navController.popBackStack() },
                                onPermissionGrantClick = { navController.navigate("camera") },
                                onLaterClick = { navController.navigate("calendar") }
                            )
                        }
                        composable("camera_detection/{imageUri}") { backStackEntry ->
                            val encodedUri = backStackEntry.arguments?.getString("imageUri")
                            val imageUri = encodedUri?.let { android.net.Uri.decode(it) }
                            if (imageUri != null) {
                                CameraDetectionPage(
                                    imageUri = imageUri,
                                    navController = navController
                                )
                            }
                        }
                        composable("camera_settings") {
                            CameraSettingsPage(
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable("manual_input") {
                            ManualInputPage(navController = navController)
                        }

                        // 캘린더 관련 페이지
                        composable("calendar") {
                            CalendarPage(navController = navController)
                        }
                        composable("personal_calorie/{date}") { backStackEntry ->
                            val date = backStackEntry.arguments?.getString("date")
                            if (date != null) {
                                PersonalCaloriePage(date = date, navController = navController)
                            }
                        }
                        composable("goal_setting") {
                            GoalSettingPage(navController = navController)
                        }

                        // 관리자 관련 페이지
                        composable("admin_login") {
                            AdminLoginPage(navController = navController)
                        }
                        composable("admin_dashboard") {
                            AdminDashboardPage(navController = navController)
                        }
                        composable("user_management") {
                            UserManagementPage(navController = navController)
                        }
                    }
                }
            }
        }
    }
}