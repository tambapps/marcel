package com.tambapps.marcel.android.marshell.ui.fav_scripts.form

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.ui.ResourceParentFragment
import com.tambapps.marcel.android.marshell.ui.editor.AbstractEditorFragment
import com.tambapps.marcel.android.marshell.view.EditTextDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScriptFormFragment: AbstractEditorFragment(), ResourceParentFragment.FabClickListener {

  companion object {
    val INVALID_CHARACTERS_REGEX = Regex("[*&%/\\\\@\"']")
    fun newInstance() = ScriptFormFragment()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.fab.visibility = View.GONE // we're using the fab of the parent fragment
  }
  override fun onFabClick() {
    if (binding.editText.text.isNullOrBlank()) {
      Toast.makeText(requireContext(), getString(R.string.script_must_not_be_empty), Toast.LENGTH_SHORT).show()
      return
    }
    EditTextDialogBuilder(requireContext())
      .setTitle("Choose script name")
      .setHint("Script name")
      .setNeutralButton("cancel")
      .setSingleLine()
      .setMaxLength(30)
      .setPositiveButton("save") { _, _, editText ->
        val text = editText.text.toString()
        if (text.isBlank()) {
          editText.error = "Name must not be blank"
          return@setPositiveButton false
        }
        if (text.contains(INVALID_CHARACTERS_REGEX)) {
          editText.error = "Name must not contain special characters"
          return@setPositiveButton false
        }
        editText.error = null
        // TODO create script
        return@setPositiveButton true
      }
      .show()
  }
}