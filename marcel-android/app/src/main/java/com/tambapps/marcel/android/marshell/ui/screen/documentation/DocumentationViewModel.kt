package com.tambapps.marcel.android.marshell.ui.screen.documentation

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.tambapps.marcel.android.marshell.repl.ShellSessionFactory
import com.tambapps.marcel.android.marshell.repl.console.SpannableHighlighter
import com.tambapps.marcel.android.marshell.service.DocumentationMdStore
import com.tambapps.marcel.android.marshell.ui.component.MarkdownComposer
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
  private val documentationStore: DocumentationMdStore,
  private val path: String?,
): ViewModel(), HighlightTransformation {
  val highlighter = SpannableHighlighter(shellSessionFactory.newReplCompiler())
  private val ioScope = CoroutineScope(Dispatchers.IO)
  var node by mutableStateOf<Node?>(null)
  private val mdComposer = MarkdownComposer(highlighter)

  @Composable
  fun Markdown(node: Node) = mdComposer.Markdown(node)

  fun fetchPage(context: Context) {
    ioScope.launch {
      val nodeResult = documentationStore.get(path)
      if (nodeResult.isFailure) {
        Toast.makeText(
          context,
          "An error occurred, please retry",
          Toast.LENGTH_SHORT
        ).show()
        return@launch
      }
      withContext(Dispatchers.Main) {
        node = nodeResult.getOrNull()
      }
    }
  }

  override fun highlight(text: CharSequence) = highlighter.highlight(text)
}