package com.tambapps.marcel.android.marshell.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import com.tambapps.marcel.android.marshell.R

val provider = GoogleFont.Provider(
  providerAuthority = "com.google.android.gms.fonts",
  providerPackage = "com.google.android.gms",
  certificates = R.array.com_google_android_gms_fonts_certs
)

val bodyFontFamily = FontFamily(
  Font(
    googleFont = GoogleFont("Roboto"),
    fontProvider = provider,
  )
)

val displayFontFamily = FontFamily(
  Font(
    googleFont = GoogleFont("Ubuntu"),
    fontProvider = provider,
  )
)

fun AppTypography(textColor: Color): Typography {
  // Default Material 3 typography values
  val baseline = Typography()
  return Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = displayFontFamily, color = textColor),
    displayMedium = baseline.displayMedium.copy(fontFamily = displayFontFamily, color = textColor),
    displaySmall = baseline.displaySmall.copy(fontFamily = displayFontFamily, color = textColor),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = displayFontFamily, color = textColor),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = displayFontFamily, color = textColor),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = displayFontFamily, color = textColor),
    titleLarge = baseline.titleLarge.copy(fontFamily = displayFontFamily, color = textColor),
    titleMedium = baseline.titleMedium.copy(fontFamily = displayFontFamily, color = textColor),
    titleSmall = baseline.titleSmall.copy(fontFamily = displayFontFamily, color = textColor),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = bodyFontFamily, color = textColor),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFontFamily, color = textColor),
    bodySmall = baseline.bodySmall.copy(fontFamily = bodyFontFamily, color = textColor),
    // shellTextStyle
    labelLarge = baseline.labelLarge.copy(fontFamily = displayFontFamily, color = textColor),
    labelMedium = baseline.labelMedium.copy(fontFamily = bodyFontFamily, color = textColor),
    labelSmall = baseline.labelSmall.copy(fontFamily = bodyFontFamily, color = textColor),
  )
}