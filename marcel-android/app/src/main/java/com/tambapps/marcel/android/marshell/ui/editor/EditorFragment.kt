package com.tambapps.marcel.android.marshell.ui.editor

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
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
import javax.inject.Inject

abstract class AbstractEditorFragment : Fragment() {

  private lateinit var editTextHighlighter: EditTextHighlighter

  @Inject
  lateinit var compilerConfiguration: CompilerConfiguration

  protected lateinit var replCompiler: MarcelReplCompiler
  private lateinit var lineCountWatcher: LineCountWatcher
  private lateinit var highlighter: SpannableHighlighter
  private val linesCount = MutableLiveData(0)
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
    lineCountWatcher = LineCountWatcher(linesCount)
    binding.editText.addTextChangedListener(lineCountWatcher)
    // not a bean because we want to keep them independent per fragment
    val marcelDexClassLoader = MarcelDexClassLoader()
    val shellBinding = Binding()
    shellBinding.setVariable("out", NoOpPrinter())
    val javaTypeResolver = ReplJavaTypeResolver(marcelDexClassLoader, shellBinding)
    replCompiler = MarcelReplCompiler(compilerConfiguration, marcelDexClassLoader, javaTypeResolver)
    highlighter = SpannableHighlighter(javaTypeResolver, replCompiler)
    editTextHighlighter = EditTextHighlighter(binding.editText, highlighter)

    if (binding.editText.requestFocus()) {
      requireContext().showSoftBoard(binding.editText)
    }
    linesCount.observe(viewLifecycleOwner) {
      binding.lineText.setText((1..(it + 1)).joinToString(separator = "\n"))
    }

    val pickFileLauncher = registerForActivityResult(FilePickerActivity.Contract()) { selectedFile: File? ->
      if (selectedFile != null) {
        onFilePicked(selectedFile)
      } else {
        Toast.makeText(requireContext(), "No file was selected", Toast.LENGTH_SHORT).show()
      }
    }
    binding.editFileButton.setOnClickListener {
      // I want .mcl files
      pickFileLauncher.launch(Intent(requireContext(), FilePickerActivity::class.java).apply {
        putExtra(FilePickerActivity.ALLOWED_FILE_EXTENSIONSKEY, FilePickerActivity.SCRIPT_FILE_EXTENSIONS)
      })
    }
    binding.fab.setOnClickListener {
      onFabClick()
    }
    binding.editFileButton.setOnLongClickListener {
      Toast.makeText(requireContext(), getString(R.string.open_file), Toast.LENGTH_SHORT).show()
      return@setOnLongClickListener true
    }
  }

  protected fun checkCompile(onSuccess: () -> Unit) {
    val dialog = ProgressDialog(requireContext()).apply {
      setTitle("Checking errors...")
    }
    dialog.show()
    CoroutineScope(Dispatchers.IO).launch {
      val result = checkCompile(dialog= dialog)
      withContext(Dispatchers.Main) {
        dialog.dismiss()
        if (result != null) { // compilation succeeded
          onSuccess.invoke()
        }
      }
    }
  }
  protected suspend fun checkCompile(scriptText: String = binding.editText.text.toString(), dialog: ProgressDialog): ReplCompilerResult? {
    return try {
      replCompiler.compile(scriptText)
    } catch (e: MarcelLexerException) {
      showScriptError(e.line, e.column, e.message, dialog)
      return null
    } catch (e: MarcelParserException) {
      showScriptError(e.line, e.column, e.message, dialog)
      return null
    } catch (e: MarcelSemanticException) {
      showScriptError(e.line, e.column, e.message, dialog)
      return null
    }
  }

  private suspend fun showScriptError(line: Int, column: Int, message: String?, dialog: ProgressDialog)
      = withContext(Dispatchers.Main) {
    dialog.dismiss()
    AlertDialog.Builder(requireContext())
      .setTitle("Compilation error")
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
  private val viewModel: EditorViewModel by viewModels()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.file.observe(viewLifecycleOwner) {
      if (it != null) {
        binding.fileNameText.text = it.name
        binding.fileNameText.visibility = View.VISIBLE
        binding.editText.setText(highlight(it.readText()))
      } else {
        binding.fileNameText.visibility = View.GONE
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
    checkCompile {
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

}