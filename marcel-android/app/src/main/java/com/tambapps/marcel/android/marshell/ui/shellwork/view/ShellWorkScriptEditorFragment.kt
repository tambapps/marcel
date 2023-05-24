package com.tambapps.marcel.android.marshell.ui.shellwork.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.tambapps.marcel.android.marshell.ui.editor.AbstractEditorFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class ShellWorkScriptEditorFragment: AbstractEditorFragment() {

  companion object {

    const val INITIAL_TEXT_KEY = "itk"
    fun newInstance(initialText: String? = null) = ShellWorkScriptEditorFragment().apply {
      if (initialText != null) {
        arguments = Bundle().apply {
          putString(INITIAL_TEXT_KEY, initialText)
        }
      }
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.fileNameText.visibility = View.VISIBLE
    binding.fileNameText.text = "Shell Work Script"
    binding.runButton.visibility = View.GONE

    val initialText = arguments?.getString(INITIAL_TEXT_KEY)
    if (initialText != null) {
      binding.editText.setText(highlight(initialText))
    }
  }

  override fun onFilePicked(selectedFile: File) {
    binding.editText.setText(highlight(selectedFile.readText()))
    Snackbar.make(binding.root, "The selected file will not be modified. A copy of it will be created", Snackbar.LENGTH_INDEFINITE)
      .setAction("ok") {
        // do nothing
      }
      .show()
  }
}