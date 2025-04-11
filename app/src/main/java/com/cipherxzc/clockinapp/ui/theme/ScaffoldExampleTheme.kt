package com.cipherxzc.clockinapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 增强颜色对比度并添加更多主题颜色
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF1EB980),
    primaryContainer = Color(0xFF045D56),
    secondary = Color(0xFF03DAC6),
    surface = Color(0xFF121212),  // 添加深色表面色
    onSurface = Color(0xFFFFFFFF) // 表面上的文字颜色
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1EB980),
    primaryContainer = Color(0xFF045D56),
    secondary = Color(0xFF03DAC6),
    error = Color(0xB2F43636),
    surface = Color(0xFFFFFFFF),  // 纯白表面色
    onSurface = Color(0xFF000000) // 黑色文字
)

// 添加自定义形状
val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

val AppTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    )
)

@Composable
fun ScaffoldExampleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
