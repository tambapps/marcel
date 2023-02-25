package com.tambapps.marcel.marshell.console

import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.console.AbstractShellCompleter
import org.jline.reader.Candidate
import org.jline.reader.Completer
import org.jline.reader.LineReader
import org.jline.reader.ParsedLine

class MarshellCompleter(
  compiler: MarcelReplCompiler,
  typeResolver: JavaTypeResolver
): AbstractShellCompleter<Candidate>(compiler, typeResolver), Completer {

  override fun complete(reader: LineReader, parsedLine: ParsedLine, candidates: MutableList<Candidate>) {
    val line = parsedLine.line()
    if (parsedLine.cursor() < line.lastIndex) return
    complete(line, candidates)
  }

  override fun newCandidate(complete: String): Candidate {
    return Candidate(complete)
  }

}