package com.tambapps.marcel.android.marshell.ui.screen.work.view

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import com.tambapps.marcel.android.marshell.repl.console.SpannableHighlighter
import com.tambapps.marcel.android.marshell.room.entity.ShellWork
import com.tambapps.marcel.android.marshell.ui.screen.HighlightTransformation
import com.tambapps.marcel.android.marshell.ui.screen.work.ScriptCardViewModel
import com.tambapps.marcel.android.marshell.work.ShellWorkManager
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.ReplMarcelSymbolResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class WorkViewModel(
  private val shellWorkManager: ShellWorkManager,
  symbolResolver: ReplMarcelSymbolResolver,
  private val replCompiler: MarcelReplCompiler,
): ViewModel(), ScriptCardViewModel {

  override var scriptTextInput by mutableStateOf(TextFieldValue())
  override var scriptTextError by mutableStateOf<String?>(null)
  override val scriptCardExpanded = mutableStateOf(false)

  var work by mutableStateOf<ShellWork?>(null)

  private val highlighter = SpannableHighlighter(symbolResolver, replCompiler)

  override fun highlight(text: CharSequence) = highlighter.highlight(text).toAnnotatedString()

  fun init(workName: String) {
    CoroutineScope(Dispatchers.IO).launch {
      val w = shellWorkManager.findByName(workName)
      if (w != null) {
        withContext(Dispatchers.Main) {
          work = w
        }
      }
    }
  }
}