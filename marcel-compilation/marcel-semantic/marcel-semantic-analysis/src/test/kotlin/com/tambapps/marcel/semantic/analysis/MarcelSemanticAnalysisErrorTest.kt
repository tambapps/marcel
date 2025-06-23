package com.tambapps.marcel.semantic.analysis

import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.parser.MarcelParser
import com.tambapps.marcel.semantic.ast.ModuleNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.processor.SourceFileSemanticProcessor
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.string.shouldContain

class MarcelSemanticAnalysisErrorTest : AnnotationSpec() {

  companion object {
    val CONFIGURATION = SemanticConfiguration()
  }

  @Test
  fun variableNotDefinedOnDeclaration() {
    val exception = shouldThrow<MarcelSemanticException> {
      apply("""
        List<int> l = [1, 2, 3]
        int i = l.find { i == 2 }
      """.trimIndent())
    }
    exception.message shouldContain "Variable i is not defined"
  }

  private fun apply(text: String): ModuleNode {
    val tokens = MarcelLexer().lex(text)
    val cst = MarcelParser(tokens).parse()
    val symbolResolver = MarcelSymbolResolver()
    return MarcelSemanticAnalysis.apply(CONFIGURATION, symbolResolver, SourceFileSemanticProcessor(symbolResolver, CONFIGURATION.scriptClass.javaType, cst, "Test.mcl"))
  }
}