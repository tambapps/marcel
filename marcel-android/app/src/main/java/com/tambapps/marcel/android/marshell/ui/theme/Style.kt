package com.tambapps.marcel.android.marshell.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tambapps.marcel.android.marshell.R

private val ubuntuFont = FontFamily(
  Font(R.font.ubuntu_font, FontWeight.Normal),
)

val shellTextStyle = TextStyle(
  fontFamily = ubuntuFont,
  fontWeight = FontWeight.Light,
  fontSize = 18.sp,
  color = Color.White
)