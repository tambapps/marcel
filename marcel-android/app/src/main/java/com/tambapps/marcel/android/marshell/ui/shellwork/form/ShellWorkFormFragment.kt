package com.tambapps.marcel.android.marshell.ui.shellwork.form

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.commitNow
import androidx.fragment.app.viewModels
import com.tambapps.marcel.android.marshell.EditorActivity
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.databinding.FragmentShellWorkFormBinding
import com.tambapps.marcel.android.marshell.service.ShellWorkManager
import com.tambapps.marcel.android.marshell.ui.shellwork.ShellWorkFragment
import com.tambapps.marcel.android.marshell.ui.shellwork.list.ShellWorkListFragment
import com.tambapps.marcel.android.marshell.room.entity.ShellWork
import com.tambapps.marcel.android.marshell.ui.shellwork.view.ShellWorkScriptEditorFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@AndroidEntryPoint
class ShellWorkFormFragment : ShellWorkFragment.ShellWorkFragmentChild() {

  companion object {
    fun newInstance(workName: String? = null) = ShellWorkFormFragment().apply {
      if (workName != null) {
        arguments = Bundle().apply {
          putString("work_name", workName)
        }
      }
    }
  }

  @Inject
  lateinit var shellWorkManager: ShellWorkManager
  private var _binding: FragmentShellWorkFormBinding? = null

  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!
  private val viewModel: ShellWorkFormViewModel by viewModels()
  private val selectedPeriodUnit get() = (binding.periodicUnitsSpinner.adapter as ArrayAdapter<PeriodUnit>).getItem(binding.periodicUnitsSpinner.selectedItemPosition)
  private var work: ShellWork? = null
  private val isCreateForm get() = work == null

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentShellWorkFormBinding.inflate(inflater, container, false)
    val root: View = binding.root

    val workName = arguments?.getString("work_name")
    if (workName != null) {
      work = runBlocking { shellWorkManager.findByName(workName) }
      if (work == null) {
        Toast.makeText(requireContext(), "Couldn't find work", Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
        return root
      }
    }

    viewModel.apply {
      scheduleTime.observe(viewLifecycleOwner) {
        binding.timeText.text = it?.toString()
      }
      scheduleDate.observe(viewLifecycleOwner) {
        binding.dateText.text = it?.toString()
      }
      viewModel.scriptText.observe(viewLifecycleOwner) {
        binding.pickScriptButton.text = requireContext().getString(if (it != null) R.string.edit_script else R.string.work_script)
      }
    }

    binding.apply {
      title.text = "New Shell Work"

      binding.workName.addTextChangedListener(object : TextWatcher {
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
        if (!isChecked) {
          viewModel.scheduleDate.value = null
          viewModel.scheduleTime.value = null
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
      periodEditText.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable) {
          val periodInt = s.toString().toIntOrNull()
          viewModel.period.value = periodInt
        }
      })

      silentCheckBox.setOnCheckedChangeListener { _, isChecked ->
        silentDescription.animate().alpha(if (isChecked) 1f else 0f).setDuration(500).start()
      }
      silentText.setOnClickListener {
        silentCheckBox.isChecked = !silentCheckBox.isChecked
      }

      pickDateButton.setOnClickListener {
        val dialog = DatePickerDialog(root.context)
        dialog.setOnDateSetListener { view, year, month, dayOfMonth ->
          // month is 0-11
          viewModel.scheduleDate.value = LocalDate.of(year, month + 1, dayOfMonth)
        }
        dialog.show()
      }
      pickTimeButton.setOnClickListener {
        val now = LocalDateTime.now()
        val dialog = TimePickerDialog(root.context, { timePicker: TimePicker, hourOfDay: Int, minutes: Int ->
          viewModel.scheduleTime.value = LocalTime.of(hourOfDay ,minutes)
        }, now.hour, now.minute, true)
        dialog.show()
      }

      periodicUnitsSpinner.adapter = ArrayAdapter(root.context, R.layout.period_unit_layout, PeriodUnit.values())
      periodicUnitsSpinner.setSelection(0)

      binding.workName.isEnabled = isCreateForm // cannot edit name of work

      val pickScriptFileLauncher = registerForActivityResult(EditorActivity.Contract()) { scriptText: String? ->
        if (scriptText != null) {
          viewModel.scriptText.value = scriptText
        } else {
          Toast.makeText(requireContext(), "No file was selected", Toast.LENGTH_SHORT).show()
        }
      }
      pickScriptButton.setOnClickListener {
        // I want .mcl files
        pickScriptFileLauncher.launch(Intent(requireContext(), EditorActivity::class.java).apply {
          if (viewModel.scriptText.value != null) {
            putExtra(ShellWorkScriptEditorFragment.TEXT_KEY, viewModel.scriptText.value)
          }
        })
      }

      if (work == null) {
        pickScriptButton.text = requireContext().getString(if (viewModel.scriptText.value != null) R.string.edit_script else R.string.work_script)
      } else {
        // if it is an update, initialize fields
        pickScriptButton.text = requireContext().getString(R.string.edit_script)
        binding.workName.setText(work!!.name)
        workDescription.setText(work!!.description)
        networkRequiredCheckBox.isChecked = work!!.isNetworkRequired
        viewModel.scheduleDate.value = work!!.scheduledAt?.toLocalDate()
        viewModel.scheduleTime.value = work!!.scheduledAt?.toLocalTime()
        viewModel.scriptText.value = work!!.scriptText
      }
    }
    return root
  }

  override fun onFabClick(): Boolean {
    if (binding.workName.text.isNullOrEmpty()) {
      binding.workName.error = getString(R.string.name_is_required)
      return false
    }
    binding.workName.error = null

    val scriptText = viewModel.scriptText.value
    if (scriptText == null) {
      Toast.makeText(activity, R.string.empty_script, Toast.LENGTH_SHORT).show()
      return false
    }
    if (binding.scheduleCheckbox.isChecked && (
      viewModel.scheduleDate.value == null || viewModel.scheduleTime.value == null)) {
      Toast.makeText(activity, R.string.didnt_filled_scheduled_parameters, Toast.LENGTH_SHORT).show()
      return false
    }

    if (binding.periodicCheckbox.isChecked && viewModel.period.value == null) {
      Toast.makeText(activity, "You must select a period", Toast.LENGTH_SHORT).show()
      return false
    }

    val name = binding.workName.text.toString()
    if (isCreateForm && shellWorkManager.existsByName(name)) {
      binding.workName.error = "Name should be unique among all active works"
      return false
    }

    // everything seems to be ok, now saving the Work
    CoroutineScope(Dispatchers.IO).launch {
      shellWorkManager.save(
        scriptText = scriptText,
        name = name,
        description = binding.workDescription.text?.toString(),
        periodAmount = binding.periodEditText.text.toString().toIntOrNull(),
        periodUnit = selectedPeriodUnit,
        networkRequired = binding.networkRequiredCheckBox.isChecked,
        silent = binding.silentCheckBox.isChecked,
        scheduleDate = viewModel.scheduleDate.value,
        scheduleTime = viewModel.scheduleTime.value
      )

      withContext(Dispatchers.Main) {
        Toast.makeText(requireContext(), "Successfully saved work", Toast.LENGTH_SHORT).show()

        if (isCreateForm) {
          // now moving back to work list
          val currentListFragment = parentFragmentManager.findFragmentByTag(ShellWorkListFragment::class.java.name)
          val fragment = currentListFragment ?: ShellWorkListFragment.newInstance()

          parentFragmentManager.commitNow {
            setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            if (currentListFragment == null) add(R.id.container, fragment, fragment.javaClass.name)
            show(fragment)
            remove(this@ShellWorkFormFragment)
          }
        } else {
          parentFragmentManager.popBackStack()
        }
      }
    }
    resourceParentFragment?.notifyNavigated()
    return true
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    val callback = object : OnBackPressedCallback(
      true // default to enabled
    ) {
      override fun handleOnBackPressed() {
        parentFab?.setImageResource(if (isCreateForm) R.drawable.plus else R.drawable.edit)
        parentFragmentManager.popBackStack()
      }
    }
    requireActivity().onBackPressedDispatcher.addCallback(
      this, // LifecycleOwner
      callback
    )
  }

}

enum class PeriodUnit {
  MINUTES, HOURS, DAYS, WEEKS, THIRTY_DAYS {
    override fun toString(): String {
      return "30 days"
    }
  };

  fun toMinutes(n: Int): Long {
    return n * when (this) {
      MINUTES -> 1L
      HOURS -> 60L
      DAYS -> HOURS.toMinutes(24)
      WEEKS -> DAYS.toMinutes(7)
      THIRTY_DAYS -> DAYS.toMinutes(30)
    }
  }

  override fun toString(): String {
    return super.toString().lowercase()
  }
}
