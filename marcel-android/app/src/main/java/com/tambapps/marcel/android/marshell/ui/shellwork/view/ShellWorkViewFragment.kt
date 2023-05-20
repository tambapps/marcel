package com.tambapps.marcel.android.marshell.ui.shellwork.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tambapps.marcel.android.marshell.databinding.FragmentShellWorkViewBinding
import com.tambapps.marcel.android.marshell.service.ShellWorkManager
import com.tambapps.marcel.android.marshell.ui.shellwork.ShellWorkFragment
import com.tambapps.marcel.android.marshell.ui.shellwork.ShellWorkTextDisplay
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

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentShellWorkViewBinding.inflate(inflater, container, false)

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    val workId = requireArguments().getString("work_id")?.let(UUID::fromString) ?: return
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
        }
      }
    }
  }

  override fun onFabClick(): Boolean {
    return false
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}