package com.tambapps.marcel.android.app.ui.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.tambapps.marcel.android.app.databinding.FragmentEditorBinding
import com.tambapps.marcel.android.app.util.ContextUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditorFragment : Fragment() {

  private val viewModel: EditorViewModel by viewModels()
  private var _binding: FragmentEditorBinding? = null
  private lateinit var lineCountWatcher:  LineCountWatcher
  private val binding get() = _binding!!

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
  }
  override fun onDestroyView() {
    binding.editText.removeTextChangedListener(lineCountWatcher)
    super.onDestroyView()
    _binding = null
  }
}