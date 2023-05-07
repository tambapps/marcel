package com.tambapps.marcel.android.marshell.ui.shellwork.form

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.tambapps.marcel.android.marshell.databinding.FragmentShellWorkFormBinding
import java.io.File

class ShellWorkFormFragment : Fragment() {

  companion object {
    fun newInstance() = ShellWorkFormFragment()
  }
  private var _binding: FragmentShellWorkFormBinding? = null

  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!
  private val viewModel: ShellWorkFormViewModel by viewModels()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {

    _binding = FragmentShellWorkFormBinding.inflate(inflater, container, false)
    val root: View = binding.root

    binding.apply {
      title.text = "New Shell Work"

      viewModel.scriptFile.observe(viewLifecycleOwner) { file: File ->
        filePath.text = file.name
      }

      workName.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable) {
          if (workNameLayout.error != null && s.toString().isNotEmpty()) {
            workNameLayout.error = null
          }
        }
      })

      networkRequiredCheckBox.setOnCheckedChangeListener { _, isChecked ->
        networkRequiredLayout.setExpanded(isChecked, true)
      }
      networkRequiredText.setOnClickListener {
        networkRequiredCheckBox.isChecked = !networkRequiredCheckBox.isChecked
      }

      scheduleCheckbox.setOnCheckedChangeListener { _, isChecked ->
        if (isChecked) {
          dateText.text = ""
          timeText.text = ""
        }
        scheduleLayout.setExpanded(isChecked, true)
      }
      scheduleLaterText.setOnClickListener {
        scheduleCheckbox.isChecked = !scheduleCheckbox.isChecked
      }

      periodicCheckbox.setOnCheckedChangeListener { _, isChecked ->
        periodicUnitsSpinner.visibility = View.VISIBLE
        periodicLayout.setExpanded(isChecked, true)
        if (!isChecked) {
          periodEditText.setText("")
        }
      }
      periodicText.setOnClickListener {
        periodicCheckbox.isChecked = !periodicCheckbox.isChecked
      }

      silentCheckBox.setOnCheckedChangeListener { _, isChecked ->
        silentDescription.animate().alpha(if (isChecked) 1f else 0f).setDuration(500).start()
      }
      silentText.setOnClickListener {
        silentCheckBox.isChecked = !silentCheckBox.isChecked
      }
    }

    return root
  }


  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}