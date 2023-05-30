package com.tambapps.marcel.android.marshell.ui.shellwork.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.ui.editor.AbstractEditorFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class ShellWorkScriptEditorFragment: AbstractEditorFragment() {

  companion object {

    const val TEXT_KEY = "itk"
    fun newInstance(initialText: String? = null) = ShellWorkScriptEditorFragment().apply {
      if (initialText != null) {
        arguments = Bundle().apply {
          putString(TEXT_KEY, initialText)
        }
      }
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.fileNameText.visibility = View.VISIBLE
    binding.fileNameText.text = "Shell Work Script"
    binding.fab.setImageResource(R.drawable.coche)

    val initialText = arguments?.getString(TEXT_KEY)
    if (initialText != null) {
      binding.editText.setText(highlight(initialText))
    }
  }

  override fun onFabClick() {
    checkCompile {
      val intent = Intent()
      // we want a string, not a serializable
      intent.putExtra(TEXT_KEY, binding.editText.text.toString())
      requireActivity().setResult(Activity.RESULT_OK, intent)
      requireActivity().finish()
    }
  }

}