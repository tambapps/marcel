package com.tambapps.marcel.repl

import com.tambapps.marcel.compiler.CompiledClass

data class ReplCompilerResult(
  val semanticResult: SemanticResult,
  val compiledScript: List<CompiledClass>,
  val otherClasses: List<CompiledClass>
) {
  val allCompiledClasses get() = compiledScript + otherClasses
}