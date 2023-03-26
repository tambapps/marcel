package com.tambapps.marcel.android.marshell.data

import marcel.lang.Binding
import java.util.Collections
import java.util.LinkedList

// TODO variables are not well handled when they are defined
class ShellSession() {
  val binding = Binding()
  // synchronized because entries are put from a non main-threads
  val history: MutableList<Prompt> = Collections.synchronizedList(LinkedList<Prompt>())

}

data class Prompt(val input: String, val output: Any?)