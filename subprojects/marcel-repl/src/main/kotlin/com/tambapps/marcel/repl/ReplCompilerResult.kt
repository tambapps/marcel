package com.tambapps.marcel.repl

import com.tambapps.marcel.compiler.CompiledClass

data class ReplCompilerResult(
  val parserResult: ParserResult,
  val compiledScript: List<CompiledClass>,
  val otherClasses: List<CompiledClass>
)