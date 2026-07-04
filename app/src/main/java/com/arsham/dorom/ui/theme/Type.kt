package com.arsham.dorom.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Body = FontFamily.SansSerif
private val Data = FontFamily.Monospace

val DoromTypography = Typography(
    displayLarge = TextStyle(fontFamily = Data, fontWeight = FontWeight.Bold, fontSize = 46.sp, letterSpacing = (-0.5).sp),
    headlineLarge = TextStyle(fontFamily = Body, fontWeight = FontWeight.Bold, fontSize = 28.sp),
    headlineMedium = TextStyle(fontFamily = Body, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleLarge = TextStyle(fontFamily = Body, fontWeight = FontWeight.SemiBold, fontSize = 19.sp),
    titleMedium = TextStyle(fontFamily = Body, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = Body, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontFamily = Body, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = Body, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = Body, fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 0.4.sp),
    labelSmall = TextStyle(fontFamily = Body, fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 0.4.sp),
)

/** Dedicated styles for numeric/data readouts (timers, percentages, scores). */
object DataText {
    val hero = TextStyle(fontFamily = Data, fontWeight = FontWeight.Bold, fontSize = 40.sp)
    val large = TextStyle(fontFamily = Data, fontWeight = FontWeight.Bold, fontSize = 26.sp)
    val medium = TextStyle(fontFamily = Data, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
    val small = TextStyle(fontFamily = Data, fontWeight = FontWeight.Medium, fontSize = 13.sp)
}
