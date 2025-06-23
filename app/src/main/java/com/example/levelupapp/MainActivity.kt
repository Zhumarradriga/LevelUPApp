package com.example.levelupapp

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
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
                                    nullable = true
                                    defaultValue = null
                                }
                            )
                        ) { EmailVerificationScreen(navController) }
                        composable("main") { MainScreen(navController) }
                        composable("profile") { ProfileScreen(navController) } // Новый экран профиля
                        composable("tasks") { TasksScreen(navController) }
                        composable("calendar") { CalendarScreen(navController) } // Заглушка для календаря
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
            primary = Color(0xFF562BD7),
            background = Color(0xFFF6F1FE),
            surface = Color(0xFFF0EBF5),
            error = Color(0xFFFF4444),
            onPrimary = Color.White,
            onBackground = Color(0xFF110730),
            onSurface = Color(0xFF110730)
        ),
        typography = Typography(
            titleLarge = TextStyle(
                fontFamily = FontFamily(Font(R.font.montserratalternatesbold)), // Добавьте шрифт Montserrat Alternates
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = FontFamily(Font(R.font.montserratalternatesregular)),
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp
            )
        ),
        content = content
    )
}

// Заглушки для новых экранов
@Composable
fun ProfileScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Экран профиля", fontSize = 24.sp)
        Button(onClick = { navController.navigate("main") }) {
            Text("Назад")
        }
    }
}



