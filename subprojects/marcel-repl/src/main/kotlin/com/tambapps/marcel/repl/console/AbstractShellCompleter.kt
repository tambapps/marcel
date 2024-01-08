package com.tambapps.marcel.repl.console

import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.ReplMarcelSymbolResolver
import java.util.*

abstract class AbstractShellCompleter<T>(
  private val compiler: MarcelReplCompiler,
  private val symbolResolver: ReplMarcelSymbolResolver
) {

  private companion object {
    val IDENTIFIER_REGEX = Regex("\\w+")
  }
  abstract fun newCandidate(complete: String): T

  fun complete(line: String, candidates: MutableList<T>) {
    val candidateStrings = TreeSet<String>()
    if (line.matches(IDENTIFIER_REGEX)) {
      symbolResolver.scriptVariables.keys.forEach { variableName ->
        if (variableName.startsWith(line)) candidateStrings.add(variableName)
      }
      compiler.definedFunctions.forEach { method ->
        if (method.name.startsWith(line)) candidateStrings.add(method.name + if (method.parameters.isEmpty()) "()" else "(")
      }
    }

    for (candidateStr in candidateStrings) {
      candidates.add(newCandidate(candidateStr))
    }
  }

}