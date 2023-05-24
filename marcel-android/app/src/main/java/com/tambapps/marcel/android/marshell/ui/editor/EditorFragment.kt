package com.tambapps.marcel.android.marshell.ui.editor

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.tambapps.marcel.android.marshell.FilePickerActivity
import com.tambapps.marcel.android.marshell.ShellHandler
import com.tambapps.marcel.android.marshell.databinding.FragmentEditorBinding
import com.tambapps.marcel.android.marshell.repl.console.SpannableHighlighter
import com.tambapps.marcel.android.marshell.util.showSoftBoard
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.repl.MarcelReplCompiler
import dagger.hilt.android.AndroidEntryPoint
import com.tambapps.marcel.android.marshell.view.EditTextHighlighter
import com.tambapps.marcel.repl.ReplJavaTypeResolver
import marcel.lang.Binding
import marcel.lang.MarcelDexClassLoader
import java.io.File
import javax.inject.Inject

abstract class AbstractEditorFragment : Fragment() {

  private lateinit var editTextHighlighter: EditTextHighlighter

  @Inject
  lateinit var compilerConfiguration: CompilerConfiguration

  private lateinit var lineCountWatcher: LineCountWatcher
  private lateinit var highlighter: SpannableHighlighter
  private val linesCount = MutableLiveData(0)
  private var _binding: FragmentEditorBinding? = null
  protected val binding get() = _binding!!

  protected abstract fun onFilePicked(selectedFile: File)
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
    val marcelDexClassLoader =
      MarcelDexClassLoader()
    val javaTypeResolver = ReplJavaTypeResolver(marcelDexClassLoader, Binding())
    val replCompiler = MarcelReplCompiler(compilerConfiguration, marcelDexClassLoader, javaTypeResolver)
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

    binding.runButton.setOnClickListener {
      val text = binding.editText.text
      if (text.isBlank()) {
        Toast.makeText(requireContext(), "Cannot run empty text", Toast.LENGTH_SHORT).show()
        return@setOnClickListener
      }
      if (shellHandler.sessionsCount <= 1) {
        doNavigateToShell(text)
      } else {
        val sessions = shellHandler.sessions
        AlertDialog.Builder(requireContext())
          .setTitle("Run in shell")
          .setItems(sessions.map { it.name }.toTypedArray()) { dialogInterface: DialogInterface, which: Int ->
            doNavigateToShell(text, which)
          }
          .show()
      }
    }
  }

  override fun onFilePicked(selectedFile: File) {
    viewModel.file.value = selectedFile
  }
  private fun doNavigateToShell(text: CharSequence, position: Int? = null) {
    shellHandler.navigateToShell(text, position)
  }

}