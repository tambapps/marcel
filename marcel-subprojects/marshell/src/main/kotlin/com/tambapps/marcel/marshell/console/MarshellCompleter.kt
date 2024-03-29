package com.tambapps.marcel.marshell.console

import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.ReplMarcelSymbolResolver
import com.tambapps.marcel.repl.console.AbstractShellCompleter
import org.jline.reader.Candidate
import org.jline.reader.Completer
import org.jline.reader.LineReader
import org.jline.reader.ParsedLine

class MarshellCompleter(
  compiler: MarcelReplCompiler,
  symbolResolver: ReplMarcelSymbolResolver
): AbstractShellCompleter<Candidate>(compiler, symbolResolver), Completer {

  override fun complete(reader: LineReader, parsedLine: ParsedLine, candidates: MutableList<Candidate>) {
    val line = parsedLine.line()
    if (parsedLine.cursor() < line.lastIndex) return
    complete(line.trim(), candidates)
  }

  override fun newCandidate(kind: Kind, value: String): Candidate {
    return Candidate(
      value, value, kind.name, null, null, null,
      // complete=false to avoid having a space at the end
      false)
  }

}