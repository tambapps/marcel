package com.tambapps.marcel.android.marshell.ui.screen

import androidx.compose.ui.text.input.TextFieldValue

interface ScriptViewModel: HighlightTransformation {

  private companion object {
    private val COMPLETION_CHARS = mapOf(
      Pair('(', ')'), Pair('{', '}'), Pair('[', ']'), Pair('\'', '\''),
      Pair('\"', '\"'))
  }

  var scriptTextInput: TextFieldValue

  fun completedInput(textFieldValue: TextFieldValue): TextFieldValue {
    /* TODO buggy sometimes, e.g. when copy/pasting a text that finish by a litteral string
    val oldText = scriptTextInput.text
    val newText = textFieldValue.text
    val newSelection = textFieldValue.selection

    if (newText.length > oldText.length) {
      // Character inserted
      val insertedCharIndex = newSelection.start - 1
      if (insertedCharIndex >= 0 && insertedCharIndex < newText.length) {
        val insertedChar = newText[insertedCharIndex]
        val completion = COMPLETION_CHARS[insertedChar]
        if (completion != null) {
          if (newSelection.start == newText.length) {
            // insertion at the end
            return textFieldValue.copy(annotatedString = textFieldValue.annotatedString + buildAnnotatedString { append(completion) })
          } else {
            // insertion in the middle or beginning
          }
        }
      }
    } else if (newText.length < oldText.length) {
      // Character removed
      val removedCharIndex = newSelection.start
      if (removedCharIndex >= 0 && removedCharIndex < oldText.length) {
        val removedChar = oldText[removedCharIndex]
        println("Character removed: $removedChar at index $removedCharIndex")
      }
    } */
    return textFieldValue
  }

  private fun completedRemove(textFieldValue: TextFieldValue, char: Char) : TextFieldValue = textFieldValue.run{
    textFieldValue
  }
}