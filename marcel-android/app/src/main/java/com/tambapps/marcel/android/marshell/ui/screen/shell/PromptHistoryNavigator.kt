package com.tambapps.marcel.android.marshell.ui.screen.shell

class PromptHistoryNavigator(private val prompts: List<Prompt>) {

  private var historyIndex = 0
  private var navigated: Boolean = false

  fun up(): CharSequence? {
    val prompts = prompts.filter { it.type == Prompt.Type.INPUT }
    if (prompts.isEmpty()) return null
    if (!navigated) {
      navigated = true
      return prompts.last().text
    }
    if (prompts.size - 1 <= historyIndex) return null
    navigated = true
    return prompts.getOrNull(prompts.size - 1 - ++historyIndex)?.text
  }

  fun down(): CharSequence? {
    val prompts = prompts.filter { it.type == Prompt.Type.INPUT }
    if (prompts.isEmpty() || !navigated || historyIndex <= -1) return null
    // get or null because historyIndex can be -1, which correspond to empty string
    val text = prompts.getOrNull(prompts.size - 1 - --historyIndex)?.text ?: ""
    navigated = true
    return text
  }

  fun reset() {
    historyIndex = 0
    navigated = false
  }
}