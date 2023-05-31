package com.tambapps.marcel.android.marshell.ui.fav_scripts.form

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.commitNow
import androidx.fragment.app.viewModels
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.service.CacheableScriptService
import com.tambapps.marcel.android.marshell.ui.ResourceParentFragment
import com.tambapps.marcel.android.marshell.ui.editor.AbstractEditorFragment
import com.tambapps.marcel.android.marshell.ui.fav_scripts.list.ScriptListFragment
import com.tambapps.marcel.android.marshell.view.EditTextDialogBuilder
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
    private const val NAME_KEY = "nk"
    fun newInstance(scriptName: String? = null) = ScriptFormFragment().apply {
      if (scriptName != null) {
        arguments = Bundle().apply {
          putString(NAME_KEY, scriptName)
        }
      }
    }
  }

  @Inject
  lateinit var scriptService: CacheableScriptService
  private val viewModel: ScriptFormViewModel by viewModels()
  private val isCreateForm get() = arguments?.getString(NAME_KEY) == null

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState) // this line is important. keep it.
    binding.fab.visibility = View.GONE // we're using the fab of the parent fragment
    viewModel.name.observe(viewLifecycleOwner) {
      if (it != null) {
        binding.fileNameText.visibility = View.VISIBLE
        binding.fileNameText.text = "$it.mcl"
      } else {
        binding.fileNameText.visibility = View.GONE
        binding.fileNameText.text = null
      }
    }

    val scriptName = arguments?.getString(NAME_KEY)
    if (scriptName != null) {
      CoroutineScope(Dispatchers.IO).launch {
        val script = scriptService.findByName(scriptName)
        withContext(Dispatchers.Main) {
          if (script == null) {
            Toast.makeText(requireContext(), "Couldn't find script", Toast.LENGTH_SHORT).show()
            backPress()
          } else {
            viewModel.name.value = script.name
            binding.editText.setText(highlight(script.text ?: ""))
          }
        }
      }
    } else {
      pickNameDialog()
    }
  }

  private fun backPress() {
    if (!parentFragmentManager.popBackStackImmediate()) requireActivity().onBackPressedDispatcher.onBackPressed()
  }

  private fun pickNameDialog(saveAfter: Boolean = false) {
    EditTextDialogBuilder(requireContext())
      .setTitle("Choose script name")
      .setHint("Script name (without file extension)")
      .setNeutralButton("cancel")
      .setSingleLine()
      .setMaxLength(30)
      .setPositiveButton("done") { _, _, editText ->
        val name = editText.text.toString()
        if (name.isBlank()) {
          editText.error = "Name must not be blank"
          return@setPositiveButton false
        }
        if (name.contains(INVALID_CHARACTERS_REGEX)) {
          editText.error = "Name must not contain special characters"
          return@setPositiveButton false
        }
        viewModel.name.value = name
        if (saveAfter) {
          checkAndSave()
        }
        return@setPositiveButton true
      }
      .setCancelable(false)
      .show()
  }

  override fun onFabClick() {
    if (isLoading) return
    if (binding.editText.text.isNullOrBlank()) {
      Toast.makeText(requireContext(), getString(R.string.script_must_not_be_empty), Toast.LENGTH_SHORT).show()
      return
    }
    checkAndSave()
  }

  private fun checkAndSave() {
    val name = viewModel.name.value
    if (name == null) {
      pickNameDialog(saveAfter = true)
      return
    }
    isLoading = true
    CoroutineScope(Dispatchers.IO).launch {
      if (isCreateForm && scriptService.existsByName(name)) {
        withContext(Dispatchers.Main) {
          Toast.makeText(requireContext(), "A script with name $name already exists", Toast.LENGTH_SHORT).show()
          isLoading = false
        }
        return@launch
      }

      val scriptText = binding.editText.text.toString()
      val compilerResult = compile()
      if (compilerResult == null) {
        withContext(Dispatchers.Main) { isLoading = false }
        return@launch
      }
      scriptService.save(name, scriptText, compilerResult)

      withContext(Dispatchers.Main) {
        isLoading = false
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
}