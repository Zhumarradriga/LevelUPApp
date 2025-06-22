package com.example.levelupapp

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.levelupapp.data.RetrofitClient

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.initialize(this)
        setContent {
            LevelUpTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "register") {
                        composable("register") { RegisterScreen(navController) }
                        composable("login") { LoginScreen(navController) }
                        composable("passwordReset") { PasswordResetScreen(navController) }
                        composable("resetPassword") { ResetPasswordScreen(navController) }
                        composable(
                            "emailVerification/{token}",
                            arguments = listOf(
                                navArgument("token") {
                                    type = NavType.StringType
                                    nullable = true // Делаем token необязательным
                                    defaultValue = null // Значение по умолчанию
                                }
                            )
                        ) { backStackEntry ->
                            EmailVerificationScreen(navController)
                        }
                        composable("main") { // Добавляем заглушку для main
                            MainScreen(navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LevelUpTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF4E27C0),
            background = Color(0xFFF6F1FE),
            surface = Color(0xFFC9BEE6),
            error = Color(0xFFFF4444),
            onPrimary = Color.White,
            onBackground = Color(0xFF4E27C0),
            onSurface = Color(0xFF75669C)
        ),
        typography = Typography(
            titleLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        ),
        content = content
    )
}