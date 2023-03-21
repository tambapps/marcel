package de.markusressel.kodehighlighter.core.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.tambapps.marcel.android.app.marcel.shell.TextViewHighlighter
import kotlinx.coroutines.*

/**
 * Convenience class for using a [LanguageRuleBook] in an [EditText]
 *
 * Note: If you need to highlight multiple [EditText] objects at the same time
 * be sure to also create one [EditTextHighlighter] instance for each [EditText].
 * Otherwise applied styles might not be cleared properly when refreshing highlighting
 * of an already highlighted [EditText].
 */
open class EditTextHighlighter(
        /**
         * The target [EditText] to apply syntax highlighting to
         */
        target: EditText,
        /**
         * The [TextViewHighlighter] to use
         */
        private val highlighter: TextViewHighlighter,
        /**
         * Time in milliseconds to debounce user input
         */
        debounceMs: Long = 100) {

    private var highlightingJob: Job? = null

    /**
     * The [Editable] to work with
     */
    val editable: Editable
        get() = target.text

    var continuousHighlight = false
        private set
    /**
     * The target [EditText] syntax highlighting is applied to
     */
    var target: EditText = target
        set(value) {
            field = value
            refreshHighlighting()
        }

    /**
     * Time in milliseconds to debounce user input
     */
    var debounceMs: Long = debounceMs
        set(value) {
            field = value
            debouncedTextWatcher.delayMs = value
        }

    private val debouncedTextWatcher = DebouncedTextWatcher(
            delayMs = this.debounceMs,
            action = {
                refreshHighlighting()
            })

    private val realtimeTextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            highlightingJob?.cancel("Text has changed")
            highlightingJob = null
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
    }

    /**
     * (Re-)Highlight the content of the [target]
     */
    open fun refreshHighlighting() {
        highlightingJob?.cancel("Requested new highlighting")
        highlightingJob = null

        highlightingJob = CoroutineScope(Dispatchers.Main).launch {
            highlighter.highlight(editable)
        }
    }

    /**
     * Create a copy of a Set
     */
    private fun <T> Set<T>.copyOf(): Set<T> {
        val original = this
        return mutableSetOf<T>().apply { addAll(original) }
    }

    /**
     * Start continuous highlighting
     */
    open fun start() {
        if (continuousHighlight) {
            return
        }
        target.addTextChangedListener(debouncedTextWatcher)
        target.addTextChangedListener(realtimeTextWatcher)
        continuousHighlight = true
    }

    /**
     * Stop continuous highlighting
     */
    open fun cancel() {
        if (!continuousHighlight) {
            return
        }
        target.removeTextChangedListener(realtimeTextWatcher)
        target.removeTextChangedListener(debouncedTextWatcher)
        continuousHighlight = false
    }
}