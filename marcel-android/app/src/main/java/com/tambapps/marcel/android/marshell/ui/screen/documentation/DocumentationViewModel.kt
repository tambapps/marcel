package com.tambapps.marcel.android.marshell.ui.screen.documentation

import androidx.lifecycle.ViewModel
import com.tambapps.marcel.android.marshell.repl.ShellSessionFactory
import com.tambapps.marcel.android.marshell.repl.console.SpannableHighlighter
import com.tambapps.marcel.android.marshell.ui.screen.HighlightTransformation
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DocumentationViewModel @Inject constructor(
  shellSessionFactory: ShellSessionFactory,
  path: String?,
): ViewModel(), HighlightTransformation {

 private val highlighter = SpannableHighlighter(shellSessionFactory.newReplCompiler())


  override fun highlight(text: CharSequence) = highlighter.highlight(text)
}