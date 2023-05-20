package com.tambapps.marcel.android.marshell.ui.shellwork.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tambapps.marcel.android.marshell.databinding.FragmentShellWorkViewBinding
import com.tambapps.marcel.android.marshell.ui.shellwork.ShellWorkFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
class ShellWorkViewFragment: ShellWorkFragment.ShellWorkFragmentChild() {
  companion object {
    fun newInstance(id: UUID) = ShellWorkViewFragment().apply {
      arguments = Bundle().apply {
        putString("work_id", id.toString())
      }
    }
  }
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
    val workId = requireArguments().getString("work_id")?.let(UUID::fromString)
    // TODO
  }
  override fun onFabClick(): Boolean {
    return false
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}