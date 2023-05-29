package com.tambapps.marcel.android.marshell.ui.fav_scripts.form

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.commitNow
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.service.CacheableScriptService
import com.tambapps.marcel.android.marshell.ui.ResourceParentFragment
import com.tambapps.marcel.android.marshell.ui.editor.AbstractEditorFragment
import com.tambapps.marcel.android.marshell.ui.fav_scripts.list.ScriptListFragment
import com.tambapps.marcel.android.marshell.view.EditTextDialogBuilder
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.exception.MarcelParserException
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ScriptFormFragment: AbstractEditorFragment(), ResourceParentFragment.FabClickListener {

  companion object {
    val INVALID_CHARACTERS_REGEX = Regex("[*&%/\\\\@\"']")
    fun newInstance() = ScriptFormFragment()
  }

  @Inject
  lateinit var scriptService: CacheableScriptService

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
        val name = editText.text.toString()
        if (name.isBlank()) {
          editText.error = "Name must not be blank"
          return@setPositiveButton false
        }
        if (name.contains(INVALID_CHARACTERS_REGEX)) {
          editText.error = "Name must not contain special characters"
          return@setPositiveButton false
        }
        editText.error = null
        checkAndSave(name)
        return@setPositiveButton true
      }
      .show()
  }

  private fun checkAndSave(name: String) {
    val dialog = ProgressDialog(requireContext()).apply {
      setTitle("Compiling and saving...")
      setCancelable(false)
      show()
    }
    CoroutineScope(Dispatchers.IO).launch {
      if (scriptService.existsByName(name)) {
        Toast.makeText(requireContext(), "A script with name $name already exists", Toast.LENGTH_SHORT).show()
        return@launch
      }

      val scriptText = binding.editText.text.toString()
      val compilerResult = try {
        replCompiler.compile(scriptText)
      } catch (e: MarcelLexerException) {
        showScriptError(e.line, e.column, e.message, dialog)
        return@launch
      } catch (e: MarcelParserException) {
        showScriptError(e.line, e.column, e.message, dialog)
        return@launch
      } catch (e: MarcelSemanticException) {
        showScriptError(e.line, e.column, e.message, dialog)
        return@launch
      }
      scriptService.create(name, scriptText, compilerResult)

      withContext(Dispatchers.Main) {
        dialog.dismiss()
        parentFragmentManager.commitNow {
          setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
          // always supplying a new fragment so that it loads the created script
          replace(R.id.container, ScriptListFragment.newInstance(), ScriptListFragment::class.java.name)
        }
        (parentFragment as? ResourceParentFragment)?.notifyNavigated(R.drawable.plus)
        Toast.makeText(requireContext(), "Script successfully created", Toast.LENGTH_SHORT).show()
      }
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
}