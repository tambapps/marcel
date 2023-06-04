package com.tambapps.marcel.android.marshell.ui.editor

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.tambapps.marcel.android.marshell.FilePickerActivity
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.ShellHandler
import com.tambapps.marcel.android.marshell.databinding.FragmentEditorBinding
import com.tambapps.marcel.android.marshell.repl.console.SpannableHighlighter
import com.tambapps.marcel.android.marshell.util.showSoftBoard
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.repl.MarcelReplCompiler
import dagger.hilt.android.AndroidEntryPoint
import com.tambapps.marcel.android.marshell.view.EditTextHighlighter
import com.tambapps.marcel.dalvik.compiler.DexException
import com.tambapps.marcel.dumbbell.DumbbellException
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.exception.MarcelParserException
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import com.tambapps.marcel.repl.ReplCompilerResult
import com.tambapps.marcel.repl.ReplJavaTypeResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import marcel.lang.Binding
import marcel.lang.MarcelDexClassLoader
import marcel.lang.printer.Printer
import java.io.File
import java.io.IOException
import javax.inject.Inject

abstract class AbstractEditorFragment : Fragment() {

  private lateinit var editTextHighlighter: EditTextHighlighter

  @Inject
  lateinit var compilerConfiguration: CompilerConfiguration

  protected lateinit var replCompiler: MarcelReplCompiler
  private lateinit var lineCountWatcher: LineCountWatcher
  private lateinit var highlighter: SpannableHighlighter
  private val viewModel: EditorViewModel by viewModels()
  var isLoading get() = viewModel.loading.value ?: false
    set(value) {
      viewModel.loading.value = value
    }

  private var _binding: FragmentEditorBinding? = null
  protected val binding get() = _binding!!

  protected open fun onFilePicked(selectedFile: File) {
    binding.editText.setText(highlight(selectedFile.readText()))
    Snackbar.make(binding.root, "The selected file won't be modified. A copy of it has been made", Snackbar.LENGTH_INDEFINITE)
      .setAction("ok") {
        // do nothing
      }
      .show()
  }
  protected abstract fun onFabClick()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentEditorBinding.inflate(inflater, container, false)
    return binding.root
  }

  protected fun highlight(text: CharSequence): Spannable {
    return highlighter.highlight(text)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    lineCountWatcher = LineCountWatcher(viewModel.linesCount)
    binding.editText.addTextChangedListener(lineCountWatcher)
    // not a bean because we want to keep them independent per fragment
    val marcelDexClassLoader = MarcelDexClassLoader()
    val shellBinding = Binding()
    val javaTypeResolver = ReplJavaTypeResolver(marcelDexClassLoader, shellBinding)
    javaTypeResolver.setScriptVariable("out", NoOpPrinter(), Printer::class.java)
    replCompiler = MarcelReplCompiler(compilerConfiguration, marcelDexClassLoader, javaTypeResolver)
    highlighter = SpannableHighlighter(javaTypeResolver, replCompiler)
    editTextHighlighter = EditTextHighlighter(binding.editText, highlighter)

    if (binding.editText.requestFocus()) {
      requireContext().showSoftBoard(binding.editText)
    }
    viewModel.linesCount.observe(viewLifecycleOwner) {
      binding.lineText.setText((1..(it + 1)).joinToString(separator = "\n"))
    }
    viewModel.loading.observe(viewLifecycleOwner) {
      if (it) {
        binding.progressBar.visibility = View.VISIBLE
        binding.editText.isEnabled = false
      } else {
        binding.progressBar.visibility = View.GONE
        binding.editText.isEnabled = true
      }
    }

    val pickFileLauncher = registerForActivityResult(FilePickerActivity.Contract()) { selectedFile: File? ->
      if (selectedFile != null) {
        onFilePicked(selectedFile)
      } else {
        Toast.makeText(requireContext(), "No file was selected", Toast.LENGTH_SHORT).show()
      }
    }
    binding.editFileButton.setOnClickListener {
      if (isLoading) return@setOnClickListener
      // I want .mcl files
      pickFileLauncher.launch(Intent(requireContext(), FilePickerActivity::class.java).apply {
        putExtra(FilePickerActivity.ALLOWED_FILE_EXTENSIONSKEY, FilePickerActivity.SCRIPT_FILE_EXTENSIONS)
      })
    }
    binding.fab.setOnClickListener {
      if (isLoading) return@setOnClickListener
      onFabClick()
    }
    binding.editFileButton.setOnLongClickListener {
      if (isLoading) return@setOnLongClickListener false
      Toast.makeText(requireContext(), getString(R.string.open_file), Toast.LENGTH_SHORT).show()
      return@setOnLongClickListener true
    }
  }

  protected fun checkCompile(onSuccess: () -> Unit) {
    viewModel.loading.value = true
    CoroutineScope(Dispatchers.IO).launch {
      val result = compile()
      withContext(Dispatchers.Main) {
        viewModel.loading.value = false
        if (result != null) { // compilation succeeded
          onSuccess.invoke()
        }
      }
    }
  }
  protected suspend fun compile(scriptText: String = binding.editText.text.toString()): ReplCompilerResult? {
    return try {
      replCompiler.compile(scriptText)
    } catch (e: MarcelLexerException) {
      showScriptError(line = e.line, column = e.column, message = e.message)
      return null
    } catch (e: MarcelParserException) {
      showScriptError(line = e.line, column = e.column, message = e.message)
      return null
    } catch (e: MarcelSemanticException) {
      showScriptError(line = e.line, column = e.column, message = e.message)
      return null
    } catch (e: DumbbellException) {
      Log.e("EditorFragment", "Dumbbell error", e)
      showScriptError(title = "Dumbbell error", message = e.message)
      return null
    } catch (e: DexException) {
      Log.e("EditorFragment", "Dex error", e)
      showScriptError(title = "Dumbbell error", message = "A fetched dumbbell jar isn't compatible with Android")
      return null
    }
  }

  private suspend fun showScriptError(line: Int? = null, column: Int? = null,
                                      title: String = "Compilation error",
                                      message: String? = null)
      = withContext(Dispatchers.Main) {
    AlertDialog.Builder(requireContext())
      .setTitle(title)
      .setMessage(message)
      .setPositiveButton("ok", null)
      .show()
  }

  override fun onResume() {
    super.onResume()
    editTextHighlighter.start()
  }

  override fun onPause() {
    super.onPause()
    editTextHighlighter.cancel()
  }
  override fun onDestroyView() {
    binding.editText.removeTextChangedListener(lineCountWatcher)
    super.onDestroyView()
    _binding = null
  }

  private class NoOpPrinter: Printer {
    override fun print(p0: CharSequence?) {}

    override fun println(p0: CharSequence?) {}

    override fun println() {}

  }

}

@AndroidEntryPoint
class EditorFragment : AbstractEditorFragment() {

  private val shellHandler get() = requireActivity() as ShellHandler
  private val viewModel: FileEditorViewModel by viewModels()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.file.observe(viewLifecycleOwner) {
      if (it != null) {
        binding.fileNameText.text = it.name
        binding.fileNameText.visibility = View.VISIBLE
        binding.editText.setText(highlight(it.readText()))
        binding.fab.setImageResource(R.drawable.save)
      } else {
        binding.fileNameText.visibility = View.GONE
        binding.fab.setImageResource(R.drawable.shell)
      }
    }
  }

  override fun onFilePicked(selectedFile: File) {
    viewModel.file.value = selectedFile
  }

  override fun onFabClick() {
    val text = binding.editText.text
    if (text.isBlank()) {
      Toast.makeText(requireContext(), "Cannot run empty text", Toast.LENGTH_SHORT).show()
      return
    }
    val file = viewModel.file.value
    if (file != null) {
      isLoading = true
      CoroutineScope(Dispatchers.IO).launch {
        try {
          file.writeText(text.toString())
          withContext(Dispatchers.Main) {
            checkCompileAndRun(text, file)
          }
        } catch (e: IOException) {
          withContext(Dispatchers.Main) {
            AlertDialog.Builder(requireContext())
              .setTitle("Error while saving file")
              .setMessage(e.message)
              .show()
          }
        }
      }
    } else {
      checkCompileAndRun(text)
    }
  }

  private fun checkCompileAndRun(text: Editable, file: File? = null) {
    checkCompile {
      if (file != null) {
        AlertDialog.Builder(requireContext())
          .setTitle("${file.name} successfully saved")
          .setMessage("No compile error were found")
          .setPositiveButton("Run in shell") { dialogInterface: DialogInterface, i: Int ->
            runInShell(text)
          }
          .setNeutralButton("ok", null)
          .show()
      } else {
        runInShell(text)
      }
    }
  }

  private fun runInShell(text: Editable) {
    if (shellHandler.sessionsCount <= 1) {
      shellHandler.navigateToShell(text)
    } else {
      val sessions = shellHandler.sessions
      AlertDialog.Builder(requireContext())
        .setTitle("Run in shell")
        .setItems(sessions.map { it.name }.toTypedArray()) { dialogInterface: DialogInterface, which: Int ->
          shellHandler.navigateToShell(text, which)
        }
        .show()
    }
  }
}