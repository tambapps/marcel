package com.tambapps.marcel.android.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.tambapps.marcel.android.app.databinding.FragmentHomeBinding
import com.tambapps.marcel.android.app.marcel.shell.AndroidMarshell
import dagger.hilt.android.AndroidEntryPoint
import marcel.lang.android.dex.MarcelDexClassLoader
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
    val printer = TextViewPrinter(requireActivity(), binding.historyText)
    marshell = AndroidMarshell(printer, marcelDexClassLoader, this::readLine)

    executor.submit {
      marshell.run()
    }


    binding.promptEditText.setOnKeyListener(PromptKeyListener(binding, printer, promptQueue))
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
    executor.shutdown()
  }

  private fun readLine(prompt: String): String {
    requireActivity().runOnUiThread {
      binding.promptText.text = "$prompt"
    }
    return promptQueue.take()
  }
}