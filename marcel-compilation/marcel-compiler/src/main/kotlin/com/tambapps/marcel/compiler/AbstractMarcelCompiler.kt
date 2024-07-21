package com.tambapps.marcel.compiler

import com.tambapps.marcel.compiler.exception.MarcelCompilerException
import com.tambapps.marcel.dumbbell.Dumbbell
import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.semantic.MarcelSemantic
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.ModuleNode
import com.tambapps.marcel.semantic.check.ClassNodeChecks
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver
import marcel.lang.MarcelClassLoader
import marcel.lang.Script

// will enrich it maybe someday if needed
abstract class AbstractMarcelCompiler(protected val configuration: CompilerConfiguration) {

  init {
    if (!Script::class.java.isAssignableFrom(configuration.scriptClass)) {
      throw MarcelSemanticException(LexToken.DUMMY, "Invalid compiler configuration: Class ${configuration.scriptClass} does not extends marcel.lang.Script")
    }
  }
  protected fun defineSymbols(symbolResolver: MarcelSymbolResolver,
                              semantics: MarcelSemantic) {
    defineSymbols(symbolResolver, listOf(semantics))
  }

  protected fun defineSymbols(symbolResolver: MarcelSymbolResolver,
                              semantics: List<MarcelSemantic>) {
    symbolResolver.defineSymbols(semantics, configuration.scriptClass.javaType)
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

  protected fun check(ast: ModuleNode, symbolResolver: MarcelSymbolResolver) {
    ast.classes.forEach { check(it, symbolResolver) }
  }

  protected fun check(classNode: ClassNode, symbolResolver: MarcelSymbolResolver) {
    ClassNodeChecks.ALL.forEach {
      it.visit(classNode, symbolResolver)
    }
    for (innerClassNode in classNode.innerClasses) {
      check(innerClassNode, symbolResolver)
    }
  }
}