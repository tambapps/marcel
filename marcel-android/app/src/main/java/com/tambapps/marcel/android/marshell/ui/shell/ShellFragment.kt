package com.tambapps.marcel.android.marshell.ui.shell

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.tambapps.marcel.android.marshell.ShellHandler
import com.tambapps.marcel.android.marshell.databinding.FragmentShellBinding
import com.tambapps.marcel.android.marshell.repl.AndroidMarshellFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShellFragment : Fragment(), TabLayoutMediator.TabConfigurationStrategy, ListUpdateCallback {

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
    val adapter = ShellWidowStateAdapter(this, shellHandler)
    binding.viewPager.adapter = adapter
    // setup tabLayout with viewPage
    TabLayoutMediator(binding.tabLayout, binding.viewPager, this).attach()

    binding.plusButton.setOnClickListener {
      if (!shellHandler.startNewSession()) {
        Toast.makeText(requireContext(), "Reach max sessions limit", Toast.LENGTH_SHORT).show()
      }
    }
    updateTabLayoutVisibility()
  }

  private fun updateTabLayoutVisibility() {
    binding.tabLayout.visibility = if (shellHandler.sessionsCount > 1) View.VISIBLE else View.GONE
  }
  fun notifySessionRemoved(i: Int) {
    binding.viewPager.adapter?.notifyItemRemoved(i)
  }

  override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
    val session = shellHandler.getSessionAt(position)
    tab.text = session.name
  }
  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  override fun onStart() {
    super.onStart()
    shellHandler.registerCallback(this)
  }

  override fun onStop() {
    shellHandler.unregisterCallback(this)
    super.onStop()
  }

  private class ShellWidowStateAdapter(fragment: Fragment, val shellHandler: ShellHandler): FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
      return shellHandler.sessionsCount
    }

    override fun createFragment(position: Int): Fragment {
      return ShellWindowFragment.newInstance(position)
    }
  }

  /// TODO there are some bugs in the below functions because they are sometimes executed while recyclevier
  //   is scrolling/computing positions
  override fun onInserted(position: Int, count: Int) {
    if (count == 1) {
      binding.viewPager.adapter?.notifyItemInserted(position)
      binding.tabLayout.getTabAt(position)?.select()
    } else {
      binding.viewPager.adapter?.notifyItemRangeInserted(position, count)
      binding.tabLayout.getTabAt(position + count - 1)?.select()
    }
    updateTabLayoutVisibility()
  }

  override fun onRemoved(position: Int, count: Int) {
    // TODO display something if there are no active sessions
    if (count == 1) {
      binding.viewPager.adapter?.notifyItemRemoved(position)
    } else {
      binding.viewPager.adapter?.notifyItemRangeRemoved(position, count)
    }
    updateTabLayoutVisibility()
  }

  override fun onMoved(fromPosition: Int, toPosition: Int) {
    binding.viewPager.adapter?.notifyItemMoved(fromPosition, toPosition)
  }

  override fun onChanged(position: Int, count: Int, payload: Any?) {
    if (count == 1) {
      binding.viewPager.adapter?.notifyItemChanged(position)
    } else {
      binding.viewPager.adapter?.notifyItemRangeChanged(position, count)
    }
  }
}