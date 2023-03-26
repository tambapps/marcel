package com.tambapps.marcel.android.marshell.ui.shell

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tambapps.marcel.android.marshell.AndroidMarshell
import com.tambapps.marcel.android.marshell.ShellHandler
import com.tambapps.marcel.android.marshell.data.ShellSession
import com.tambapps.marcel.android.marshell.databinding.FragmentShellWindowBinding
import com.tambapps.marcel.android.marshell.repl.AndroidMarshellFactory
import com.tambapps.marcel.android.marshell.util.showSoftBoard
import dagger.hilt.android.AndroidEntryPoint
import com.tambapps.marcel.android.marshell.view.EditTextHighlighter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import marcel.lang.MarcelSystem
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import javax.inject.Inject


@AndroidEntryPoint
class ShellWindowFragment : Fragment() {

  companion object {
    const val POSITION_KEY = "p"
    fun newInstance(position: Int) = ShellWindowFragment().apply {
      arguments = Bundle().apply {
        putInt(POSITION_KEY, position)
      }
    }
  }
  private var _binding: FragmentShellWindowBinding? = null
  @Inject
  lateinit var factory: AndroidMarshellFactory
  private val binding get() = _binding!!

  // TODO move this in a MarcelRunner class to abstract executor and everything
  private lateinit var marshell: AndroidMarshell
  private lateinit var printer: TextViewPrinter
  private lateinit var editTextHighlighter: EditTextHighlighter
  private lateinit var executor: ExecutorService
  private lateinit var promptQueue: LinkedBlockingQueue<CharSequence>
  private var position = 0
  lateinit var shellSession: ShellSession
  private lateinit var shellHandler: ShellHandler


  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentShellWindowBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    executor = Executors.newSingleThreadExecutor()
    promptQueue = LinkedBlockingQueue<CharSequence>()
    position = requireArguments().getInt(POSITION_KEY)
    shellHandler = requireActivity() as ShellHandler
    shellSession = shellHandler.shellSessions[position]

    printer = TextViewPrinter(requireActivity(), binding.historyText)
    marshell = factory.newShell(printer, shellSession.binding, this::readLine)
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
          requireContext().showSoftBoard(v)
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