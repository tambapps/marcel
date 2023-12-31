package com.tambapps.marcel.repl.console

import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver

abstract class AbstractShellCompleter<T>(
  private val compiler: MarcelReplCompiler,
  private val symbolResolver: MarcelSymbolResolver
) {

  abstract fun newCandidate(complete: String): T

  fun complete(lastLine: String, candidates: MutableList<T>) {
    // TODO
  }

}