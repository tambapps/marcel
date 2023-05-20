package com.tambapps.marcel.android.marshell.ui.shellwork.form

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.commitNow
import androidx.fragment.app.viewModels
import com.tambapps.marcel.android.marshell.FilePickerActivity
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.databinding.FragmentShellWorkFormBinding
import com.tambapps.marcel.android.marshell.service.ShellWorkManager
import com.tambapps.marcel.android.marshell.ui.shellwork.ShellWorkFragment
import com.tambapps.marcel.android.marshell.ui.shellwork.list.ShellWorkListFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@AndroidEntryPoint
class ShellWorkFormFragment : ShellWorkFragment.ShellWorkFragmentChild() {

  companion object {
    fun newInstance() = ShellWorkFormFragment()
  }

  @Inject
  lateinit var shellWorkManager: ShellWorkManager
  private var _binding: FragmentShellWorkFormBinding? = null

  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!
  private val viewModel: ShellWorkFormViewModel by viewModels()
  private val selectedPeriodUnit get() = (binding.periodicUnitsSpinner.adapter as ArrayAdapter<PeriodUnit>).getItem(binding.periodicUnitsSpinner.selectedItemPosition)

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentShellWorkFormBinding.inflate(inflater, container, false)
    val root: View = binding.root

    viewModel.apply {
      scheduleTime.observe(viewLifecycleOwner) {
        binding.timeText.text = it?.toString()
      }
      scheduleDate.observe(viewLifecycleOwner) {
        binding.dateText.text = it?.toString()
      }
      viewModel.scriptFile.observe(viewLifecycleOwner) {
        binding.filePath.text = it?.name
      }
    }

    binding.apply {
      title.text = "New Shell Work"

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

      val pickScriptFileLauncher = registerForActivityResult(FilePickerActivity.Contract()) { selectedFile: File? ->
        if (selectedFile != null) {
          viewModel.scriptFile.value = selectedFile
        } else {
          Toast.makeText(requireContext(), "No file was selected", Toast.LENGTH_SHORT).show()
        }
      }
      pickScriptButton.setOnClickListener {
        // I want .mcl files
        pickScriptFileLauncher.launch(Intent(requireContext(), FilePickerActivity::class.java).apply {
          putExtra(FilePickerActivity.ALLOWED_FILE_EXTENSIONSKEY, FilePickerActivity.SCRIPT_FILE_EXTENSIONS)
        })
      }

      periodicUnitsSpinner.setSelection(0)
    }
    return root
  }

  override fun onFabClick(): Boolean {
    if (binding.workName.text.isNullOrEmpty()) {
      binding.workName.error = getString(R.string.name_is_required)
      return false
    }
    binding.workName.error = null

    val scriptFile = viewModel.scriptFile.value
    if (scriptFile == null) {
      Toast.makeText(activity, R.string.must_select_script, Toast.LENGTH_SHORT).show()
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
    if (shellWorkManager.existsByName(name)) {
      Toast.makeText(activity, "Name should be unique among all active works", Toast.LENGTH_SHORT).show()
      return false
    }

    // everything seems to be ok, now creating the Work
    CoroutineScope(Dispatchers.IO).launch {
      shellWorkManager.create(
        scriptFile = scriptFile,
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
        Toast.makeText(requireContext(), "Successfully created work", Toast.LENGTH_SHORT).show()

        // now moving back to work list
        val currentListFragment = parentFragmentManager.findFragmentByTag(ShellWorkListFragment::class.java.name)
            as? ShellWorkListFragment?
        currentListFragment?.refreshWorks() // to refresh data when going back to page
        val fragment = currentListFragment ?: ShellWorkListFragment.newInstance()

        parentFragmentManager.commitNow {
          setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
          if (currentListFragment == null) add(R.id.container, fragment, fragment.javaClass.name)
          show(fragment)
          remove(this@ShellWorkFormFragment)
        }
      }
    }
    (parentFragment as? ShellWorkFragment)?.notifyNavigated()
    return true
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
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
