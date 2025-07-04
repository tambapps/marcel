package com.tambapps.marcel.semantic.analysis

import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.parser.MarcelParser
import com.tambapps.marcel.semantic.ast.ModuleNode
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.processor.SourceFileSemanticProcessor
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.symbol.type.NullSafetyMode

object TestUtils {

  private val CONFIGURATION = SemanticConfiguration()
  fun applySemantic(text: String, nullSafetyMode: NullSafetyMode? = null): ModuleNode {
    val tokens = MarcelLexer().lex(text)
    val cst = MarcelParser(tokens).parse()
    val symbolResolver = MarcelSymbolResolver()
    return MarcelSemanticAnalysis.apply(
      if (nullSafetyMode != null) CONFIGURATION.copy(nullSafetyMode = nullSafetyMode) else CONFIGURATION,
      symbolResolver, SourceFileSemanticProcessor(symbolResolver, CONFIGURATION.scriptClass.javaType, cst, "Test.mcl"))
  }

}