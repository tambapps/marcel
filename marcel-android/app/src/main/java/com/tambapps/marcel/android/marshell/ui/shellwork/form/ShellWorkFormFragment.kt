package com.tambapps.marcel.android.marshell.ui.shellwork.form

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.fragment.app.viewModels
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.tambapps.marcel.android.marshell.FilePickerActivity
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.databinding.FragmentShellWorkFormBinding
import com.tambapps.marcel.android.marshell.ui.shellwork.ShellWorkFragment
import com.tambapps.marcel.android.marshell.work.MarcelShellWorker
import com.tambapps.marcel.android.marshell.work.WorkTags
import java.io.File
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class ShellWorkFormFragment : ShellWorkFragment.ShellWorkFragmentChild() {

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

    viewModel.apply {
      scheduleTime.observe(viewLifecycleOwner) {
        binding.timeText.text = it?.toString()
      }
      scheduleDate.observe(viewLifecycleOwner) {
        binding.dateText.text = it?.toString()
      }
      period.observe(viewLifecycleOwner) {
        binding.periodicText.text = it?.toString()
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

  override fun onFabClick() {
    if (binding.workName.text.isNullOrEmpty()) {
      binding.workName.error = getString(R.string.name_is_required)
      return
    }
    binding.workName.error = null

    val scriptFile = viewModel.scriptFile.value
    if (scriptFile == null) {
      Toast.makeText(activity, R.string.must_select_script, Toast.LENGTH_SHORT).show()
      return
    }
    if (binding.scheduleCheckbox.isChecked && (
      viewModel.scheduleDate.value == null || viewModel.scheduleTime.value == null)) {
      Toast.makeText(activity, R.string.didnt_filled_scheduled_parameters, Toast.LENGTH_SHORT).show()
      return
    }

    if (binding.periodicCheckbox.isChecked && viewModel.period.value == null) {
      Toast.makeText(activity, "You must select a period", Toast.LENGTH_SHORT).show()
      return
    }

    // everything seems to be ok, now creating the Work
    doSaveWork(
      scriptFile = scriptFile,
      name = binding.workName.text.toString(),
      description = binding.workDescription.text?.toString(),
      periodAmount = binding.periodEditText.text.toString().toLongOrNull(),
      periodUnit = (binding.periodicUnitsSpinner.adapter as ArrayAdapter<PeriodUnit>).getItem(binding.periodicUnitsSpinner.selectedItemPosition),
      networkRequired = binding.networkRequiredCheckBox.isChecked,
      silent = binding.silentCheckBox.isChecked,
      scheduleDate = viewModel.scheduleDate.value,
      scheduleTime = viewModel.scheduleTime.value
      )
    Toast.makeText(requireContext(), "Successfully created work", Toast.LENGTH_SHORT).show()
  }

  private fun doSaveWork(periodAmount: Long?, periodUnit: PeriodUnit?, name: String, scriptFile: File,
                         description: String?, networkRequired: Boolean, silent: Boolean,
                         scheduleDate: LocalDate?, scheduleTime: LocalTime?) {
    val workRequest: WorkRequest.Builder<*, *> =
      if (periodAmount != null && periodUnit != null) PeriodicWorkRequestBuilder<MarcelShellWorker>(
        periodUnit.toMinutes(periodAmount), TimeUnit.MINUTES)
        .addTag(WorkTags.periodAmount(periodAmount))
        .addTag(WorkTags.periodUnit(periodUnit))
      else OneTimeWorkRequest.Builder(MarcelShellWorker::class.java)

    if (scheduleDate != null && scheduleTime != null) {
      val scheduleDateTime = LocalDateTime.of(scheduleDate, scheduleTime)
      workRequest.setInitialDelay(Duration.ofMillis(
        LocalDateTime.now().until(scheduleDateTime, ChronoUnit.MILLIS)))
        .addTag(WorkTags.schedule(scheduleDateTime.toString()))
    }
    if (networkRequired) {
      val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
      workRequest.setConstraints(constraints)
    }

    workRequest.apply {
      addTag(WorkTags.type(WorkTags.SHELL_WORK_TYPE))
      addTag(WorkTags.name(name))
      addTag(WorkTags.silent(silent))
      addTag(WorkTags.networkRequired(networkRequired))
      if (!description.isNullOrBlank()) {
        addTag(WorkTags.description(description))
      }
      addTag(WorkTags.scriptPath(scriptFile.absolutePath))
      setInputData(Data.Builder().build())
    }

    val workManager = WorkManager.getInstance(requireActivity())
    val operation = if (workRequest is PeriodicWorkRequest.Builder) workManager.enqueueUniquePeriodicWork(name, ExistingPeriodicWorkPolicy.REPLACE, workRequest.build())
    else workManager.enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, workRequest.build() as OneTimeWorkRequest)
    // waiting for the work to be created
    operation.result.get()
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

  fun toMinutes(n: Long): Long {
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
