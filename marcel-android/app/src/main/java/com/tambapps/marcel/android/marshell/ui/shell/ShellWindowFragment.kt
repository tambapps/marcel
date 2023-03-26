package com.tambapps.marcel.android.marshell.ui.shell

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tambapps.marcel.android.marshell.ShellHandler
import com.tambapps.marcel.android.marshell.data.ShellSession
import com.tambapps.marcel.android.marshell.databinding.FragmentShellWindowBinding
import com.tambapps.marcel.android.marshell.repl.AndroidMarshell
import com.tambapps.marcel.android.marshell.repl.AndroidMarshellFactory
import com.tambapps.marcel.android.marshell.repl.AndroidMarshellRunner
import com.tambapps.marcel.android.marshell.util.showSoftBoard
import dagger.hilt.android.AndroidEntryPoint
import com.tambapps.marcel.android.marshell.view.EditTextHighlighter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import marcel.lang.MarcelSystem
import marcel.lang.util.MarcelVersion
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

  private lateinit var marshellRunner: AndroidMarshellRunner
  private lateinit var printer: TextViewPrinter
  private lateinit var editTextHighlighter: EditTextHighlighter
  private lateinit var promptQueue: LinkedBlockingQueue<CharSequence>
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
    promptQueue = LinkedBlockingQueue<CharSequence>()
    val position = requireArguments().getInt(POSITION_KEY)
    shellHandler = requireActivity() as ShellHandler
    shellSession = shellHandler.shellSessions[position]

    printer = TextViewPrinter(requireActivity(), binding.historyText)
    marshellRunner = factory.newShellRunner(shellSession, printer, this::readLine)
    val highlighter = marshellRunner.shell.newHighlighter()
    editTextHighlighter = EditTextHighlighter(binding.promptEditText, highlighter)

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

    printer.println("Marshell (Marcel: ${MarcelVersion.VERSION}, Android ${Build.VERSION.RELEASE})\n")
    // now display history if any
    if (shellSession.history.isNotEmpty()) {
      val historyTextView = binding.historyText
      for (prompt in shellSession.history) {
        prompt.input.lines().forEachIndexed { index, promptLine ->
          historyTextView.append(AndroidMarshell.PROMPT_TEMPLATE.format(index))
          binding.historyText.append(highlighter.highlight(promptLine))
          binding.historyText.append("\n")
        }
        historyTextView.append("${prompt.output}\n")
      }
    }
  }

  override fun onStart() {
    super.onStart()
    marshellRunner.start()
    // TODO put this in onShow because we can have different windows
    MarcelSystem.setPrinter(printer)
    editTextHighlighter.start()
  }
  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
    marshellRunner.stop()
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