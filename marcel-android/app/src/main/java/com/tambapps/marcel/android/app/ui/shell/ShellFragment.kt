package com.tambapps.marcel.android.app.ui.shell

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.tambapps.marcel.android.app.databinding.FragmentShellBinding
import com.tambapps.marcel.android.app.marcel.shell.AndroidMarshell
import dagger.hilt.android.AndroidEntryPoint
import de.markusressel.kodehighlighter.core.util.EditTextHighlighter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import marcel.lang.MarcelSystem
import marcel.lang.android.dex.MarcelDexClassLoader
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import javax.inject.Inject


@AndroidEntryPoint
class ShellFragment : Fragment() {

  private var _binding: FragmentShellBinding? = null


  @Inject
  lateinit var marcelDexClassLoader: MarcelDexClassLoader
  private val binding get() = _binding!!

  private lateinit var marshell: AndroidMarshell
  private lateinit var printer: TextViewPrinter
  private lateinit var editTextHighlighter: EditTextHighlighter
  private lateinit var executor: ExecutorService
  private lateinit var promptQueue: LinkedBlockingQueue<CharSequence>

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentShellBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    executor = Executors.newSingleThreadExecutor()
    promptQueue = LinkedBlockingQueue<CharSequence>()

    printer = TextViewPrinter(requireActivity(), binding.historyText)
    marshell = AndroidMarshell(printer, marcelDexClassLoader, this::readLine)
    val highlighter = marshell.newHighlighter()
    editTextHighlighter = EditTextHighlighter(binding.promptEditText, highlighter)

    executor.submit {
      runBlocking {
        marshell.printVersion()
        marshell.run()
      }
    }
    binding.apply {
      promptEditText.setOnKeyListener(PromptKeyListener(promptQueue))
      promptEditText.setOnFocusChangeListener { v, hasFocus ->
        if (hasFocus) {
          val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
          imm.showSoftInput(v, 0)
        }
      }
      promptEditText.requestFocus()
      historyText.setOnClickListener {
        promptEditText.requestFocus()
      }
    }
  }

  override fun onStart() {
    // TODO move this kind of thing in a MarcelEngine class
    super.onStart()
    MarcelSystem.setPrinter(printer)
    editTextHighlighter.start()
  }
  override fun onDestroyView() {
    super.onDestroyView()
    runBlocking { marshell.exit() }
    _binding = null
    executor.shutdown()
  }

  override fun onStop() {
    super.onStop()
    // TODO move this kind of thing in a MarcelEngine class
    MarcelSystem.setPrinter(null)
    editTextHighlighter.cancel()
  }
  private suspend fun readLine(prompt: String): String {
    withContext(Dispatchers.Main) {
      binding.historyText.append(prompt)
      binding.fakePromptText.text = prompt
    }
    val text = promptQueue.take()
    withContext(Dispatchers.Main) {
      printer.println(text)
      binding.promptEditText.setText("")
    }
    return text.toString()
  }
}