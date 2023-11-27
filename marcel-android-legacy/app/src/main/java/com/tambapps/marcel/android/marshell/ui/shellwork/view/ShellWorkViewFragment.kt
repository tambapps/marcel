package com.tambapps.marcel.android.marshell.ui.shellwork.view

import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.commit
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.databinding.FragmentShellWorkViewBinding
import com.tambapps.marcel.android.marshell.room.entity.ShellWork
import com.tambapps.marcel.android.marshell.service.ShellWorkManager
import com.tambapps.marcel.android.marshell.ui.shellwork.ShellWorkFragment
import com.tambapps.marcel.android.marshell.ui.shellwork.ShellWorkTextDisplay
import com.tambapps.marcel.android.marshell.ui.shellwork.form.ShellWorkFormFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ShellWorkViewFragment: ShellWorkFragment.ShellWorkFragmentChild(R.drawable.plus), ShellWorkTextDisplay {
  companion object {
    const val SHELL_WORK_NAME_KEY = "work_name"
    fun newInstance(workName: String) = ShellWorkViewFragment().apply {
      arguments = Bundle().apply {
        putString(SHELL_WORK_NAME_KEY, workName)
      }
    }
  }

  @Inject
  lateinit var shellWorkManager: ShellWorkManager
  private var _binding: FragmentShellWorkViewBinding? = null
  private val binding get() = _binding!!
  val workName get() = requireArguments().getString(SHELL_WORK_NAME_KEY)

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentShellWorkViewBinding.inflate(inflater, container, false)

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    refresh()
    registerPeriodicCallback(this::refresh)
    if (parentFragment == null) {
      binding.fab.visibility = View.GONE
    }
  }

  private fun refresh() {
    val workName = this.workName ?: return
    CoroutineScope(Dispatchers.IO).launch {
      val work = shellWorkManager.findByName(workName) ?: return@launch
      withContext(Dispatchers.Main) {
        display(work)
      }
    }
  }

  private fun display(work: ShellWork) {
    // using _binding to avoid trying to modify views even though the fragment has been destroyed
    _binding?.apply {
      nameText.text = work.name
      descriptionText.visibility = if (work.description.isNullOrBlank()) View.GONE else View.VISIBLE
      descriptionText.text = work.description
      displayWork(context = requireContext(), work = work,
        name = nameText, result = resultText,
        startTime = startTimeText, state = stateText, nextRun = nextRunText, singleLineStateText = true)
      consultLogsButton.visibility = if (work.logs.isNullOrBlank()) View.GONE else View.VISIBLE
      consultLogsButton.setOnClickListener {
        val view = LayoutInflater.from(requireContext())
          .inflate(R.layout.dialog_logs, null)
        view.findViewById<TextView>(R.id.textView).text = work.logs
        AlertDialog.Builder(requireContext())
          .setView(view)
          .setNeutralButton("ok", null)
          .show()
      }

      fab.backgroundTintList = ColorStateList.valueOf(if (work.isFinished) Color.RED else requireContext().getColor(R.color.orange))
      fab.setOnClickListener {
        if (parentFragment == null) return@setOnClickListener
        if (!work.isFinished) {
          AlertDialog.Builder(requireContext()).setTitle(requireContext().getString(R.string.cancel_work_q, work.name))
            .setNeutralButton(android.R.string.no, null)
            .setPositiveButton("yes") { _: DialogInterface, _: Int ->
              CoroutineScope(Dispatchers.IO).launch {
                shellWorkManager.cancel(work.name)
                withContext(Dispatchers.Main) {
                  Toast.makeText(requireContext(), "Successfully canceled work", Toast.LENGTH_SHORT).show()
                }
              }
            }.create()
            .apply {
              setOnShowListener {
                getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(requireContext().getColor(R.color.orange))
              }
            }.show()
        } else {
          AlertDialog.Builder(requireContext()).setTitle(R.string.delete_shell_work)
            .setMessage(requireContext().getString(R.string.delete_work_q, work.name))
            .setNeutralButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _: DialogInterface, _: Int ->
              CoroutineScope(Dispatchers.IO).launch {
                if (!shellWorkManager.delete(work.name)) return@launch

                withContext(Dispatchers.Main) {
                  Toast.makeText(requireContext(), "Successfully deleted work", Toast.LENGTH_SHORT).show()
                  parentFragmentManager.popBackStack()
                }
              }

            }
            .create()
            .apply {
              setOnShowListener {
                getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
              }
            }.show()
        }
      }
    }
  }

  override fun onFabClick() {
    val workName = this.workName ?: return
    navigateTo(ShellWorkFormFragment.newInstance(workName = workName), R.drawable.save)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}