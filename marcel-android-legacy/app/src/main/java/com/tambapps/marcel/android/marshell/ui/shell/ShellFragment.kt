package com.tambapps.marcel.android.marshell.ui.shell

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.viewpager2.adapter.MyFragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.tambapps.marcel.android.marshell.FilePickerActivity
import com.tambapps.marcel.android.marshell.ShellHandler
import com.tambapps.marcel.android.marshell.databinding.FragmentShellBinding
import com.tambapps.marcel.android.marshell.repl.AndroidMarshellFactory
import dagger.hilt.android.AndroidEntryPoint
import marcel.lang.MarcelSystem
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ShellFragment : Fragment(), TabLayoutMediator.TabConfigurationStrategy, ListUpdateCallback {

  companion object {
    const val CACHED_SCRIPT_NAME_ARG = "cachedScriptName"
    const val SCRIPT_TEXT_ARG = "scriptText"
    const val SESSION_INDEX_ARG = "sessionIndex"
  }
  private var _binding: FragmentShellBinding? = null
  private val binding get() = _binding!!
  @Inject
  lateinit var factory: AndroidMarshellFactory
  lateinit var shellHandler: ShellHandler
  private var adapter: ShellWidowStateAdapter? = null


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
    val scriptText = arguments?.getCharSequence(SCRIPT_TEXT_ARG)
    val sessionIndex = arguments?.getInt(SESSION_INDEX_ARG, 0) ?: 0
    val cachedScriptName = arguments?.getString(CACHED_SCRIPT_NAME_ARG)
    val args = if (scriptText != null || cachedScriptName != null) ShellFragmentArguments(cachedScriptName, scriptText, sessionIndex) else null

    val adapter = ShellWidowStateAdapter(this, shellHandler, args)
    this.adapter = adapter
    binding.viewPager.adapter = adapter
    // setup tabLayout with viewPage
    TabLayoutMediator(binding.tabLayout, binding.viewPager, this).attach()
    if (sessionIndex != 0 && sessionIndex < shellHandler.sessionsCount) {
      binding.tabLayout.selectTab(binding.tabLayout.getTabAt(sessionIndex))
    }

    binding.plusButton.setOnClickListener {
      if (!shellHandler.startNewSession()) {
        Toast.makeText(requireContext(), "Reach max sessions limit", Toast.LENGTH_SHORT).show()
      }
    }
    val pickScriptResultLauncher = registerForActivityResult(FilePickerActivity.Contract()) { selectedFile: File? ->
      if (selectedFile != null) {
        val fileText = selectedFile.readText()
        val fragment = adapter.getFragmentAt(binding.viewPager.currentItem) as ShellSessionFragment
        fragment.runScript(fileText)
      } else {
        Toast.makeText(requireContext(), "No file was selected", Toast.LENGTH_SHORT).show()
      }
    }

    binding.runFileButton.setOnClickListener {
      // I want .mcl files
      pickScriptResultLauncher.launch(Intent(requireContext(), FilePickerActivity::class.java).apply {
        putExtra(FilePickerActivity.ALLOWED_FILE_EXTENSIONSKEY, FilePickerActivity.SCRIPT_FILE_EXTENSIONS)
      })
    }
    binding.exportButton.setOnClickListener {
      // TODO export current session as a file
      Toast.makeText(requireContext(), "TODO", Toast.LENGTH_SHORT).show()
    }

    binding.exportButton.setOnLongClickListener {
      Toast.makeText(requireContext(), "Export session to a file", Toast.LENGTH_SHORT).show()
      return@setOnLongClickListener true
    }
    binding.runFileButton.setOnLongClickListener {
      Toast.makeText(requireContext(), "Run script", Toast.LENGTH_SHORT).show()
      return@setOnLongClickListener true
    }
    binding.plusButton.setOnLongClickListener {
      Toast.makeText(requireContext(), "Start a new shell", Toast.LENGTH_SHORT).show()
      return@setOnLongClickListener true
    }
    updateTabLayoutVisibility()
  }

  private fun updateTabLayoutVisibility() {
    binding.tabLayout.visibility = if (shellHandler.sessionsCount > 1) View.VISIBLE else View.GONE
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

  private class ShellWidowStateAdapter(
    fragment: Fragment,
    val shellHandler: ShellHandler,
    val arguments: ShellFragmentArguments?
    ): MyFragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
      return shellHandler.sessionsCount
    }

    override fun createFragment(position: Int): Fragment {
      return ShellSessionFragment.newInstance(position= position, scriptText = arguments?.scriptText,
        cachedScriptName= arguments?.cachedScriptName)
    }

    override fun onBindFragment(fragment: Fragment, position: Int) {
      (fragment as ShellSessionFragment).bindTo(shellHandler.getSessionAt(position))
    }
  }

  override fun onInserted(position: Int, count: Int) {
    binding.viewPager.adapter?.notifyItemRangeInserted(position, count)
    binding.tabLayout.getTabAt(position + count - 1)?.select()
    updateTabLayoutVisibility()
  }

  override fun onRemoved(position: Int, count: Int) {
    // TODO display something if there are no active sessions
    Log.d(javaClass.simpleName, "on shell removed position=$position, count=$count")
    binding.viewPager.adapter?.notifyItemRangeRemoved(position, count)
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

  private fun getCurrentPrinter(): TextViewPrinter? {
    val fragment = adapter?.getFragmentAt(binding.viewPager.currentItem) as? ShellSessionFragment
    return fragment?.printer
  }
}

data class ShellFragmentArguments(
  val cachedScriptName: String?,
  val scriptText: CharSequence?,
  val sessionIndex: Int
) {
}