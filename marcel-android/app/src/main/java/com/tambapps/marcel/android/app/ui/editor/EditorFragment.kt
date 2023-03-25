package com.tambapps.marcel.android.app.ui.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.tambapps.marcel.android.app.databinding.FragmentEditorBinding
import com.tambapps.marcel.android.app.marcel.shell.TextViewHighlighter
import com.tambapps.marcel.android.app.util.ContextUtils
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.repl.MarcelReplCompiler
import dagger.hilt.android.AndroidEntryPoint
import de.markusressel.kodehighlighter.core.util.EditTextHighlighter
import marcel.lang.MarcelDexClassLoader
import javax.inject.Inject

@AndroidEntryPoint
class EditorFragment : Fragment() {

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
      ContextUtils.showSoftBoard(requireContext(), binding.editText)
    }
    viewModel.linesCount.observe(requireActivity()) {
      binding.lineText.text = (1..(it + 1)).joinToString(separator = "\n")
    }

    // not a bean because we want to keep them independent per fragment
    val marcelDexClassLoader =
      MarcelDexClassLoader()
    val javaTypeResolver = JavaTypeResolver(marcelDexClassLoader)
    val replCompiler = MarcelReplCompiler(compilerConfiguration, javaTypeResolver)
    val highlighter = TextViewHighlighter(javaTypeResolver, replCompiler)
    editTextHighlighter = EditTextHighlighter(binding.editText, highlighter)
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