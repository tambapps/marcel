package com.tambapps.marcel.android.marshell.ui.screen.documentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.tambapps.marcel.android.marshell.repl.ShellSessionFactory
import com.tambapps.marcel.android.marshell.repl.console.SpannableHighlighter
import com.tambapps.marcel.android.marshell.ui.screen.HighlightTransformation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import javax.inject.Inject

@HiltViewModel
class DocumentationViewModel @Inject constructor(
  shellSessionFactory: ShellSessionFactory,
  path: String?,
): ViewModel(), HighlightTransformation {
  private val parser = Parser.builder().build()
  private val okhttp = OkHttpClient()
  private val highlighter = SpannableHighlighter(shellSessionFactory.newReplCompiler())
  private val ioScope = CoroutineScope(Dispatchers.IO)
  private val url = "https://raw.githubusercontent.com/tambapps/marcel/main/documentation/src" + (
      path ?: "/marcel.md"
      )
  var node by mutableStateOf<Node?>(null)

  fun fetchPage() {
    ioScope.launch {
      okhttp.newCall(Request.Builder().get().url(url).get().build()).execute().use { response ->
        if (response.isSuccessful && response.body != null) {
          withContext(Dispatchers.Main) {
            node = parser.parse(response.body!!.string())
          }
        } else {
          println()
          // TODO handle error
        }
      }
    }
  }
  override fun highlight(text: CharSequence) = highlighter.highlight(text)
}