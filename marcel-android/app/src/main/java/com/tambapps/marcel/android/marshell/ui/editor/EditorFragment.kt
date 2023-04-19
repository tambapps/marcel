package com.tambapps.marcel.android.marshell.ui.editor

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
import javax.inject.Inject

@AndroidEntryPoint
class EditorFragment : Fragment() {

  private val shellHandler get() = requireActivity() as ShellHandler
  private val viewModel: EditorViewModel by viewModels()

  private lateinit var editTextHighlighter: EditTextHighlighter

  @Inject
  lateinit var compilerConfiguration: CompilerConfiguration

  private var _binding: FragmentEditorBinding? = null
  private val binding get() = _binding!!

  private lateinit var lineCountWatcher: LineCountWatcher

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentEditorBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    lineCountWatcher = LineCountWatcher(viewModel.linesCount)
    binding.editText.addTextChangedListener(lineCountWatcher)

    if (binding.editText.requestFocus()) {
      requireContext().showSoftBoard(binding.editText)
    }
    val lineText = binding.lineText
    viewModel.linesCount.observe(viewLifecycleOwner) {
      lineText.text = (1..(it + 1)).joinToString(separator = "\n")
    }

    // not a bean because we want to keep them independent per fragment
    val marcelDexClassLoader =
      MarcelDexClassLoader()
    val javaTypeResolver = ReplJavaTypeResolver(marcelDexClassLoader, Binding())
    val replCompiler = MarcelReplCompiler(compilerConfiguration, marcelDexClassLoader, javaTypeResolver)
    val highlighter = SpannableHighlighter(javaTypeResolver, replCompiler)
    editTextHighlighter = EditTextHighlighter(binding.editText, highlighter)
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

  private fun doNavigateToShell(text: CharSequence, position: Int? = null) {
    shellHandler.navigateToShell(text, position)
  }

  override fun onStart() {
    super.onStart()
    editTextHighlighter.start()
  }

  override fun onDestroyView() {
    binding.editText.removeTextChangedListener(lineCountWatcher)
    editTextHighlighter.cancel()
    super.onDestroyView()
    _binding = null
  }
}