package com.tambapps.marcel.compiler

import com.tambapps.marcel.compiler.exception.MarcelCompilerException
import com.tambapps.marcel.dumbbell.Dumbbell
import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.SourceFileNode
import com.tambapps.marcel.semantic.MarcelSemantic
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.ModuleNode
import com.tambapps.marcel.semantic.check.ClassNodeChecks
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import com.tambapps.marcel.semantic.type.SymbolsDefiner
import marcel.lang.MarcelClassLoader
import marcel.lang.Script

// will enrich it maybe someday if needed
abstract class AbstractMarcelCompiler(protected val configuration: CompilerConfiguration) {

  init {
    if (!Script::class.java.isAssignableFrom(configuration.scriptClass)) {
      throw MarcelSemanticException(LexToken.DUMMY, "Invalid compiler configuration: Class ${configuration.scriptClass} does not extends marcel.lang.Script")
    }
  }
  protected fun defineSymbols(typeResolver: JavaTypeResolver,
                              semantics: MarcelSemantic) {
    defineSymbols(typeResolver, listOf(semantics))
  }

  protected fun defineSymbols(typeResolver: JavaTypeResolver,
                              semantics: List<MarcelSemantic>) {
    SymbolsDefiner(typeResolver, configuration.scriptClass.javaType).defineSymbols(semantics)
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

  protected fun check(ast: ModuleNode, typeResolver: JavaTypeResolver) {
    ast.classes.forEach { check(it, typeResolver) }
  }

  protected fun check(classNode: ClassNode, typeResolver: JavaTypeResolver) {
    ClassNodeChecks.ALL.forEach {
      it.visit(classNode, typeResolver)
    }
    for (innerClassNode in classNode.innerClasses) {
      check(innerClassNode, typeResolver)
    }
  }
}