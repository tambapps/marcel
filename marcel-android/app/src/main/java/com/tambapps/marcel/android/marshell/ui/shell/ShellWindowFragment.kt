package com.tambapps.marcel.android.marshell.ui.shell

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
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
import kotlinx.coroutines.withContext
import marcel.lang.MarcelSystem
import marcel.lang.util.MarcelVersion
import java.util.concurrent.LinkedBlockingQueue
import javax.inject.Inject

@AndroidEntryPoint
class ShellWindowFragment : Fragment() {

  companion object {
    const val POSITION_KEY = "p"
    fun newInstance(position: Int, scriptText: CharSequence?) = ShellWindowFragment().apply {
      arguments = Bundle().apply {
        putInt(POSITION_KEY, position)
        if (scriptText != null) {
          putCharSequence(ShellFragment.SCRIPT_TEXT_ARG, scriptText)
        }
      }
    }
  }
  private var _binding: FragmentShellWindowBinding? = null
  @Inject
  lateinit var factory: AndroidMarshellFactory
  private val binding get() = _binding!!

  private var marshellRunner: AndroidMarshellRunner? = null
  private var editTextHighlighter: EditTextHighlighter? = null
  private lateinit var printer: TextViewPrinter
  private lateinit var promptQueue: LinkedBlockingQueue<CharSequence>
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
    shellHandler = requireActivity() as ShellHandler
    printer = TextViewPrinter(requireActivity(), binding.historyText)

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

    val scriptText = requireArguments().getCharSequence(ShellFragment.SCRIPT_TEXT_ARG)
    if (scriptText != null) {
      val spanString = SpannableString("// imported script")
      spanString.setSpan(ForegroundColorSpan(Color.LTGRAY), 0, spanString.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
      promptQueue.add(spanString)
      promptQueue.add(scriptText)
    }
  }

  override fun onStart() {
    super.onStart()
    // TODO put this in onShow because we can have different windows
    MarcelSystem.setPrinter(printer)
  }
  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
    marshellRunner?.stop()
  }

  override fun onStop() {
    super.onStop()
    // TODO move this kind of thing in a MarcelEngine class
    MarcelSystem.setPrinter(null)
    editTextHighlighter?.cancel()
  }
  private suspend fun readLine(prompt: String): String {
    withContext(Dispatchers.Main) {
      binding.historyText.append(prompt)
      binding.fakePromptText.text = prompt
    }
    val text = promptQueue.take()
    if (text.none { it == '\n' }) { // in order not to display imported scripts which can be long
      withContext(Dispatchers.Main) {
        printer.println(text)
        binding.promptEditText.setText("")
      }
    }
    return text.toString()
  }

  fun bindTo(shellSession: ShellSession) {
    marshellRunner?.stop()
    marshellRunner = factory.newShellRunner(shellSession, printer, this::readLine) {
      shellHandler.stopSession(shellSession)
    }
    marshellRunner?.start()
    val highlighter = marshellRunner!!.shell.newHighlighter()
    editTextHighlighter?.cancel()
    editTextHighlighter = EditTextHighlighter(binding.promptEditText, highlighter)
    editTextHighlighter?.start()
    val historyTextView = binding.historyText
    historyTextView.text = ""

    printer.println("Marshell (Marcel: ${MarcelVersion.VERSION}, Android ${Build.VERSION.RELEASE})\n")
    // now display history if any
    if (shellSession.history.isNotEmpty()) {
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
}