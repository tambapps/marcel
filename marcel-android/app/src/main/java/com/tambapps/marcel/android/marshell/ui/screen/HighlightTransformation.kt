package com.tambapps.marcel.android.marshell.ui.screen

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

interface HighlightTransformation: VisualTransformation {

  fun highlight(text: CharSequence): AnnotatedString

  override fun filter(text: AnnotatedString): TransformedText {
    return TransformedText(
      text = highlight(text),
      offsetMapping = OffsetMapping.Identity
    )
  }

}