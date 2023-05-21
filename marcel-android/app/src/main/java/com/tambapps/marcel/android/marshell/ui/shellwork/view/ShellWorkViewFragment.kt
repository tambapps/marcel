package com.tambapps.marcel.android.marshell.ui.shellwork.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.commit
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.databinding.FragmentShellWorkViewBinding
import com.tambapps.marcel.android.marshell.service.ShellWorkManager
import com.tambapps.marcel.android.marshell.ui.shellwork.ShellWorkFragment
import com.tambapps.marcel.android.marshell.ui.shellwork.ShellWorkTextDisplay
import com.tambapps.marcel.android.marshell.ui.shellwork.form.ShellWorkFormFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class ShellWorkViewFragment: ShellWorkFragment.ShellWorkFragmentChild(), ShellWorkTextDisplay {
  companion object {
    fun newInstance(id: UUID) = ShellWorkViewFragment().apply {
      arguments = Bundle().apply {
        putString("work_id", id.toString())
      }
    }
  }

  @Inject
  lateinit var shellWorkManager: ShellWorkManager
  private var _binding: FragmentShellWorkViewBinding? = null
  private val binding get() = _binding!!
  val workId get() = requireArguments().getString("work_id")?.let(UUID::fromString)

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentShellWorkViewBinding.inflate(inflater, container, false)

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    val workId = this.workId ?: return
    CoroutineScope(Dispatchers.IO).launch {
      val work = shellWorkManager.findById(workId) ?: return@launch
      withContext(Dispatchers.Main) {
        binding.apply {
          nameText.text = work.name
          descriptionText.visibility = if (work.description.isNullOrBlank()) View.GONE else View.VISIBLE
          descriptionText.text = work.description
          displayWork(context = requireContext(), work = work,
            name = nameText, result = resultText,
            startTime = startTimeText, state = stateText, nextRun = nextRunText, singleLineStateText = true)
          consultLogsButton.visibility = if (work.logs.isNullOrBlank()) View.GONE else View.VISIBLE
          consultLogsButton.setOnClickListener {
            Toast.makeText(requireContext(), "TODO", Toast.LENGTH_SHORT).show()
          }
        }
      }
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    val callback = object : OnBackPressedCallback(
      true // default to enabled
    ) {
      override fun handleOnBackPressed() {
        fab?.setImageResource(R.drawable.plus)
        parentFragmentManager.popBackStack()
      }
    }
    requireActivity().onBackPressedDispatcher.addCallback(
      this, // LifecycleOwner
      callback
    )
  }
  override fun onFabClick(): Boolean {
    val workId = this.workId ?: return false

    val fragment = ShellWorkFormFragment.newInstance(id = workId)
    parentFragmentManager.commit {
      setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
      // to handle back press
      addToBackStack(null)
      add(R.id.container, fragment, fragment.javaClass.name)
      show(fragment)
      hide(this@ShellWorkViewFragment)
    }
    shellWorkFragment?.notifyNavigated(R.drawable.save)
    return false // returning false because we want to modify the fab's icon with notifyNavigated
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}