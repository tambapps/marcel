package com.tambapps.marcel.compiler

import com.tambapps.marcel.compiler.exception.MarcelCompilerException
import com.tambapps.marcel.dumbbell.Dumbbell
import com.tambapps.marcel.parser.cst.SourceFileNode
import com.tambapps.marcel.semantic.MarcelSemantic
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import com.tambapps.marcel.semantic.type.SymbolsDefiner
import marcel.lang.MarcelClassLoader

// will enrich it maybe someday if needed
abstract class AbstractMarcelCompiler(protected val configuration: CompilerConfiguration) {

  protected fun defineSymbols(typeResolver: JavaTypeResolver,
                              semantics: MarcelSemantic) {
    defineSymbols(typeResolver, listOf(semantics))
  }

  protected fun defineSymbols(typeResolver: JavaTypeResolver,
                              semantics: List<MarcelSemantic>) {
    SymbolsDefiner(typeResolver).defineSymbols(semantics)
  }
  protected fun handleDumbbells(marcelClassLoader: MarcelClassLoader?, cst: SourceFileNode) {
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
    artifacts.forEach {
      if (it.jarFile != null) { // can be null as there could be pom-only artifacs
        marcelClassLoader.addLibraryJar(it.jarFile)
      }
    }
  }
}