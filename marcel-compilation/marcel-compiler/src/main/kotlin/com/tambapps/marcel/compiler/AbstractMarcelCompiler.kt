package com.tambapps.marcel.compiler

import com.tambapps.marcel.compiler.exception.MarcelCompilerException
import com.tambapps.marcel.dumbbell.Dumbbell
import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import marcel.lang.MarcelClassLoader
import marcel.lang.Script

// will enrich it maybe someday if needed
abstract class AbstractMarcelCompiler(protected val configuration: CompilerConfiguration) {

  init {
    if (!Script::class.java.isAssignableFrom(configuration.semanticConfiguration.scriptClass)) {
      throw MarcelSemanticException(LexToken.DUMMY, "Invalid compiler configuration: Class ${configuration.semanticConfiguration.scriptClass} does not extends marcel.lang.Script")
    }
  }

  protected fun handleDumbbells(marcelClassLoader: MarcelClassLoader?, cst: SourceFileCstNode) {
    if (cst.dumbbells.isNotEmpty()) {
      if (!configuration.dumbbellEnabled) {
        throw MarcelCompilerException("Cannot use dumbbells because dumbbell feature is not enabled")
      }
      if (marcelClassLoader == null) {
        throw MarcelCompilerException("Cannot use dumbbells because no class loader was provided")
      }
      cst.dumbbells.forEach { handleDumbbell(marcelClassLoader, it) }
    }
  }

  protected fun handleDumbbell(marcelClassLoader: MarcelClassLoader, dumbbell: String) {
    val artifacts = Dumbbell.pull(dumbbell)
    // jarFile can be null as there could be pom-only artifacts
    marcelClassLoader.addJars(artifacts.mapNotNull { it.jarFile })
  }
}