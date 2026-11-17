package dz.univ.alger1.heartcareai.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Cardio = Color(0xFFE65368)
val CardioDark = Color(0xFFBD344D)
val CardioSoft = Color(0xFFFFE9ED)
val Mint = Color(0xFF0F9F8F)
val MintSoft = Color(0xFFE5F6F2)
val Ink = Color(0xFF1D2939)
val InkSoft = Color(0xFF334155)
val Slate = Color(0xFF64748B)
val PageBg = Color(0xFFF6F8FB)
val BorderLight = Color(0xFFE5EAF1)
val AmberSoft = Color(0xFFFFF4E2)

private val HeartScheme = lightColorScheme(
    primary = Cardio,
    onPrimary = Color.White,
    primaryContainer = CardioSoft,
    onPrimaryContainer = CardioDark,
    secondary = Mint,
    onSecondary = Color.White,
    secondaryContainer = MintSoft,
    onSecondaryContainer = Ink,
    background = PageBg,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF0F3F7),
    onSurfaceVariant = Slate,
    outline = BorderLight,
    error = CardioDark
)

private val HeartTypography = Typography(
    headlineLarge = TextStyle(fontSize = 31.sp, lineHeight = 37.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 25.sp, lineHeight = 31.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontSize = 22.sp, lineHeight = 29.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 27.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 23.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 23.sp),
    bodyMedium = TextStyle(fontSize = 13.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold)
)

@Composable
fun HeartCareTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = HeartScheme, typography = HeartTypography, content = content)
}
