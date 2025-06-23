package com.tambapps.marcel.semantic.analysis

import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.ModuleNode
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.processor.MarcelSemantic
import com.tambapps.marcel.semantic.processor.check.ClassNodeChecks
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.transform.SyntaxTreeTransformer

/**
 * Class to perform Marcel semantic analysis
 */
object MarcelSemanticAnalysis {

  fun apply(configuration: SemanticConfiguration, symbolResolver: MarcelSymbolResolver, semantics: MarcelSemantic): ModuleNode {
    return apply(configuration, symbolResolver, listOf(semantics)).first()
  }

  fun apply(configuration: SemanticConfiguration, symbolResolver: MarcelSymbolResolver, semantics: List<MarcelSemantic>): List<ModuleNode> {
    // defining types
    symbolResolver.defineSymbols(semantics, configuration.scriptClass.javaType)

    // load transformations if any
    val syntaxTreeTransformer = SyntaxTreeTransformer(configuration.purpose, symbolResolver)
    semantics.forEach { syntaxTreeTransformer.applyCstTransformations(it) }

    // apply semantic analysis
    val asts = semantics.map { it.apply() }

    // apply transformations if any
    asts.forEach { syntaxTreeTransformer.applyAstTransformations(it) }

    // checks
    asts.forEach { ast ->
      ast.classes.forEach { check(it, symbolResolver) }
    }
    return asts
  }

  private fun check(classNode: ClassNode, symbolResolver: MarcelSymbolResolver) {
    ClassNodeChecks.ALL.forEach {
      it.visit(classNode, symbolResolver)
    }
    for (innerClassNode in classNode.innerClasses) {
      check(innerClassNode, symbolResolver)
    }
  }
}