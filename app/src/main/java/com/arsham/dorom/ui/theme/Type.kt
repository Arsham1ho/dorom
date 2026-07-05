package com.arsham.dorom.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.arsham.dorom.R

// Poppins (body/headlines) and JetBrains Mono (numeric/data readouts) — both bundled locally
// (OFL-licensed, see THIRD_PARTY_FONT_LICENSES_*.txt at the repo root) so there's no runtime
// download and no dependency on Google Play Services being present.
private val Body = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
    Font(R.font.poppins_black, FontWeight.Black),
)
private val Data = FontFamily(
    Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
    Font(R.font.jetbrains_mono_semibold, FontWeight.SemiBold),
    Font(R.font.jetbrains_mono_bold, FontWeight.Bold),
)

val DoromTypography = Typography(
    displayLarge = TextStyle(fontFamily = Data, fontWeight = FontWeight.Bold, fontSize = 46.sp, letterSpacing = (-0.5).sp),
    headlineLarge = TextStyle(fontFamily = Body, fontWeight = FontWeight.Black, fontSize = 28.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontFamily = Body, fontWeight = FontWeight.Black, fontSize = 22.sp, letterSpacing = (-0.3).sp),
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
