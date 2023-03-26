package com.tambapps.marcel.android.marshell.ui.shell

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tambapps.marcel.android.marshell.ShellHandler
import com.tambapps.marcel.android.marshell.data.ShellSession
import com.tambapps.marcel.android.marshell.databinding.FragmentShellBinding
import com.tambapps.marcel.android.marshell.repl.AndroidMarshellFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShellFragment : Fragment() {

  private var _binding: FragmentShellBinding? = null
  private val binding get() = _binding!!
  @Inject
  lateinit var factory: AndroidMarshellFactory
  lateinit var shellHandler: ShellHandler


  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentShellBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    shellHandler = requireActivity() as ShellHandler
    binding.viewPager.adapter = ShellWidowStateAdapter(this, shellHandler.shellSessions)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  private class ShellWidowStateAdapter(fragment: Fragment, val sessions: List<ShellSession>): FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
      return sessions.size
    }

    override fun createFragment(position: Int): Fragment {
      return ShellWindowFragment.newInstance(position)
    }

  }
}