package com.tambapps.marcel.android.marshell.service

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tambapps.marcel.android.marshell.Routes
import com.tambapps.marcel.android.marshell.repl.ShellSessionFactory
import com.tambapps.marcel.android.marshell.repl.console.SpannableHighlighter
import com.tambapps.marcel.android.marshell.ui.screen.editor.EditorViewModel
import com.tambapps.marcel.android.marshell.ui.screen.shell.ShellViewModel
import com.tambapps.marcel.android.marshell.ui.screen.work.create.WorkCreateViewModel
import com.tambapps.marcel.android.marshell.ui.screen.work.list.WorksListViewModel
import com.tambapps.marcel.android.marshell.ui.screen.work.view.WorkViewModel
import com.tambapps.marcel.android.marshell.work.ShellWorkManager
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.ReplMarcelSymbolResolver
import marcel.lang.Binding
import marcel.lang.MarcelDexClassLoader
import javax.inject.Inject

class ViewModelFactory @Inject constructor(
  private val compilerConfiguration: CompilerConfiguration,
  private val shellSessionFactory: ShellSessionFactory,
  private val shellWorkManager: ShellWorkManager
): ViewModelProvider.Factory {

  @Composable
  inline fun <reified VM : ViewModel> newInstance(): VM = viewModel(factory = this)

  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return create(modelClass, CreationExtras.Empty)
  }

  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
    return when (modelClass) {
      EditorViewModel::class.java -> {
        val classLoader = MarcelDexClassLoader()
        val symbolResolver = ReplMarcelSymbolResolver(classLoader, Binding())
        val replCompiler = MarcelReplCompiler(compilerConfiguration, classLoader, symbolResolver)
        EditorViewModel(symbolResolver, replCompiler)
      }
      ShellViewModel::class.java -> ShellViewModel(shellSessionFactory.newSession())
      WorkCreateViewModel::class.java -> {
        val classLoader = MarcelDexClassLoader()
        val symbolResolver = ReplMarcelSymbolResolver(classLoader, Binding())
        val replCompiler = MarcelReplCompiler(compilerConfiguration, classLoader, symbolResolver)
        WorkCreateViewModel(shellWorkManager, symbolResolver, replCompiler)
      }
      WorkViewModel::class.java -> {
        val classLoader = MarcelDexClassLoader()
        val symbolResolver = ReplMarcelSymbolResolver(classLoader, Binding())
        val replCompiler = MarcelReplCompiler(compilerConfiguration, classLoader, symbolResolver)
        // argument should be accessible because of android compose navigation
        val workName = extras.createSavedStateHandle().get<String>(Routes.WORK_NAME_ARG)
        WorkViewModel(shellWorkManager, symbolResolver, replCompiler, workName)
      }
      WorksListViewModel::class.java -> WorksListViewModel(shellWorkManager)
      else -> throw UnsupportedOperationException("Cannot create ViewModel of class $modelClass")
    } as T
  }

  private fun newSpannableHighlighter(): SpannableHighlighter {
    val classLoader = MarcelDexClassLoader()
    val symbolResolver = ReplMarcelSymbolResolver(classLoader, Binding())
    val replCompiler = MarcelReplCompiler(compilerConfiguration, classLoader, symbolResolver)
    return SpannableHighlighter(symbolResolver, replCompiler)
  }
}