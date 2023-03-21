package com.tambapps.marcel.android.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tambapps.marcel.android.app.databinding.FragmentHomeBinding
import com.tambapps.marcel.android.app.marcel.shell.AndroidMarshell
import dagger.hilt.android.AndroidEntryPoint
import marcel.lang.MarcelSystem
import marcel.lang.android.dex.MarcelDexClassLoader
import marcel.lang.printer.Printer
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

  private var _binding: FragmentHomeBinding? = null


  @Inject
  lateinit var marcelDexClassLoader: MarcelDexClassLoader
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  private lateinit var marshell: AndroidMarshell
  private lateinit var printer: Printer
  private val executor = Executors.newSingleThreadExecutor()
  private val promptQueue = LinkedBlockingQueue<String>()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {

    _binding = FragmentHomeBinding.inflate(inflater, container, false)
    /*
    val homeViewModel =
      ViewModelProvider(this).get(HomeViewModel::class.java)

    val textView: TextView = binding.textHome
    homeViewModel.text.observe(viewLifecycleOwner) {
      textView.text = it
    }
     */
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    printer = TextViewPrinter(requireActivity(), binding.historyText)
    marshell = AndroidMarshell(printer, marcelDexClassLoader, this::readLine)

    marshell.printVersion()
    executor.submit {
      marshell.run()
    }
    binding.apply {
      promptEditText.setOnKeyListener(PromptKeyListener(binding, printer, promptQueue))
      promptEditText.requestFocus()
    }
  }

  override fun onStart() {
    // TODO move this kind of thing in a MarcelEngine class
    super.onStart()
    MarcelSystem.setPrinter(printer)
  }
  override fun onDestroyView() {
    super.onDestroyView()
    marshell.exit()
    _binding = null
    executor.shutdown()
  }

  override fun onStop() {
    super.onStop()
    // TODO move this kind of thing in a MarcelEngine class
    MarcelSystem.setPrinter(null)
  }
  private fun readLine(prompt: String): String {
    requireActivity().runOnUiThread {
      binding.promptText.text = "$prompt"
    }
    return promptQueue.take()
  }
}