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
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.tambapps.marcel.android.marshell.ShellHandler
import com.tambapps.marcel.android.marshell.data.ShellSession
import com.tambapps.marcel.android.marshell.databinding.FragmentShellSessionBinding
import com.tambapps.marcel.android.marshell.repl.AndroidMarshell
import com.tambapps.marcel.android.marshell.repl.AndroidMarshellFactory
import com.tambapps.marcel.android.marshell.repl.AndroidMarshellRunner
import com.tambapps.marcel.android.marshell.service.CacheableScriptService
import com.tambapps.marcel.android.marshell.util.showSoftBoard
import dagger.hilt.android.AndroidEntryPoint
import com.tambapps.marcel.android.marshell.view.EditTextHighlighter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import marcel.lang.printer.Printer
import marcel.lang.util.MarcelVersion
import java.util.concurrent.LinkedBlockingQueue
import javax.inject.Inject

@AndroidEntryPoint
class ShellSessionFragment : Fragment() {

  companion object {
    private const val POSITION_KEY = "p"
    fun newInstance(position: Int, scriptText: CharSequence?, cachedScriptName: String?) = ShellSessionFragment().apply {
      arguments = Bundle().apply {
        putInt(POSITION_KEY, position)
        if (scriptText != null) {
          putCharSequence(ShellFragment.SCRIPT_TEXT_ARG, scriptText)
        }
        if (cachedScriptName != null) {
          putCharSequence(ShellFragment.CACHED_SCRIPT_NAME_ARG, cachedScriptName)
        }
      }
    }
  }

  private var _binding: FragmentShellSessionBinding? = null
  @Inject
  lateinit var factory: AndroidMarshellFactory
  @Inject
  lateinit var scriptService: CacheableScriptService
  private val binding get() = _binding!!

  private var marshellRunner: AndroidMarshellRunner? = null
  private var editTextHighlighter: EditTextHighlighter? = null
  lateinit var printer: TextViewPrinter
  private lateinit var promptQueue: LinkedBlockingQueue<CharSequence>
  private lateinit var shellHandler: ShellHandler

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentShellSessionBinding.inflate(inflater, container, false)
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
      if (promptEditText.requestFocus()) {
        requireContext().showSoftBoard(promptEditText)
      }

      historyText.setOnClickListener {
        if (promptEditText.requestFocus()) {
          requireContext().showSoftBoard(promptEditText)
        }
      }
    }

    val scriptText = requireArguments().getCharSequence(ShellFragment.SCRIPT_TEXT_ARG)
    if (scriptText != null) {
      runScript(scriptText)
    }
    println("cacacacacaaca " + requireArguments().getString(ShellFragment.CACHED_SCRIPT_NAME_ARG))
    val cachedScriptName = requireArguments().getString(ShellFragment.CACHED_SCRIPT_NAME_ARG)
    if (cachedScriptName != null) {
      runCachedScript(cachedScriptName)
    }
  }

  fun runCachedScript(cachedScriptName: String) {
    CoroutineScope(Dispatchers.IO).launch {
      val cacheableScript = scriptService.findByNameWithJar(cachedScriptName)
      withContext(Dispatchers.Main) {
        if (cacheableScript == null) {
          Toast.makeText(requireContext(), "Script couldn't be found", Toast.LENGTH_SHORT).show()
          return@withContext
        }
        if (marshellRunner == null) {
          Toast.makeText(
            requireContext(),
            "Shell isn't running, couldn't run the script",
            Toast.LENGTH_SHORT
          ).show()
        } else {
          marshellRunner!!.evalCachedScript(cacheableScript)
        }
      }
    }
  }

  fun runScript(scriptText: CharSequence) {
    val spanString = SpannableString("// imported script")
    spanString.setSpan(ForegroundColorSpan(Color.LTGRAY), 0, spanString.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
    promptQueue.add(spanString)
    val highlightedText =
      if (scriptText !is Spannable && marshellRunner != null) marshellRunner!!.shell.newHighlighter().highlight(scriptText)
      else scriptText
    promptQueue.add(highlightedText)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
    marshellRunner?.stop()
  }

  override fun onResume() {
    super.onResume()
    editTextHighlighter?.start()
  }

  override fun onPause() {
    super.onPause()
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
    // dispose things if any
    marshellRunner?.stop()
    marshellRunner = factory.newShellRunner(shellSession, printer, this::readLine) {
      shellHandler.stopSession(shellSession)
    }

    // then start things
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
    shellSession.typeResolver.setScriptVariable("out", SessionPrinter(printer), Printer::class.java)
  }

  // this is because we don't want to pass a SuspendPrinter directly to the shell, which has other methods like suspendPrint
  private class SessionPrinter(private val printer: Printer): Printer {
    override fun print(p0: CharSequence?) {
      printer.print(p0)
    }

    override fun print(o: Any?) {
      printer.print(o)
    }

    override fun println(o: Any?) {
      printer.println(o)
    }
    override fun println(p0: CharSequence?) {
      printer.print(p0)
    }

    override fun println() {
      printer.println()
    }

    override fun toString(): String {
      return "Marshell Session Printer"
    }
  }
}