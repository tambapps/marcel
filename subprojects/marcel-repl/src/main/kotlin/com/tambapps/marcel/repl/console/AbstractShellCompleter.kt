package com.tambapps.marcel.repl.console

import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.ReplMarcelSymbolResolver

abstract class AbstractShellCompleter<T: Comparable<T>>(
  private val compiler: MarcelReplCompiler,
  private val symbolResolver: ReplMarcelSymbolResolver
) {

  private companion object {
    val IDENTIFIER_REGEX = Regex("\\w+")
  }

  enum class Kind {
    METHODS, FIELDS
  }

  abstract fun newCandidate(kind: Kind, value: String): T

  fun complete(line: String, candidates: MutableList<T>) {
    if (line.matches(IDENTIFIER_REGEX)) {
      symbolResolver.scriptVariables.keys.forEach { variableName ->
        if (variableName.startsWith(line)) add(candidates, newCandidate(Kind.FIELDS, variableName))
      }
      compiler.definedFunctions.forEach { method ->
        if (method.name.startsWith(line)) add(candidates, newCandidate(Kind.METHODS, method.name + if (method.parameters.isEmpty()) "()" else "("))
      }
    }
  }

  private fun add(candidates: MutableList<T>, c: T) {
    var i = 0
    while (i < candidates.size && candidates[i] < c) i++
    candidates.add(i, c)
  }
}