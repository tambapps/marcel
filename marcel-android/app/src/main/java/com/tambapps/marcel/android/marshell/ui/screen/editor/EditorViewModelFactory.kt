package com.tambapps.marcel.android.marshell.ui.screen.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tambapps.marcel.android.marshell.repl.console.SpannableHighlighter
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.ReplMarcelSymbolResolver
import marcel.lang.Binding
import marcel.lang.MarcelDexClassLoader
import javax.inject.Inject

class EditorViewModelFactory @Inject constructor(
  private val compilerConfiguration: CompilerConfiguration
): ViewModelProvider.Factory {

  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    val classLoader = MarcelDexClassLoader()
    val symbolResolver = ReplMarcelSymbolResolver(classLoader, Binding())
    val replCompiler = MarcelReplCompiler(compilerConfiguration, classLoader, symbolResolver)
    return EditorViewModel(SpannableHighlighter(symbolResolver, replCompiler)) as T
  }
}
