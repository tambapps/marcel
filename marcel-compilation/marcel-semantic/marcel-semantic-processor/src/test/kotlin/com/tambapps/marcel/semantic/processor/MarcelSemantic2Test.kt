package com.tambapps.marcel.semantic.processor

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.compose.StatementScope
import com.tambapps.marcel.parser.cst.IdentifiableCstNode
import com.tambapps.marcel.parser.compose.CstInstructionComposer as CstComposer
import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNode
import com.tambapps.marcel.semantic.ast.IdentifiableAstNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.compose.AstStatementScope
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.processor.TestUtils.assertIsEqual
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.symbol.type.NullSafetyMode
import marcel.lang.Script
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MarcelSemantic2Test: AstStatementScope() {

  @Test
  fun testConstants() {
    assertIsEqual(int(123)) { int(123) }
  }

  fun assertIsEqual(expected: IdentifiableAstNode, cstComposer: StatementScope.() -> IdentifiableCstNode) {
    val cst = cstComposer.invoke(StatementScope())
    val actual = when (cst) {
      is ExpressionCstNode ->  cst.accept(processor())
      is StatementCstNode ->  cst.accept(processor())
      else -> throw RuntimeException()
    }
    assertTrue(expected.isEqualTo(actual), "Expected $actual to be equal to $expected")
  }

  private fun processor(): SourceFileSemanticProcessor {
    return SourceFileSemanticProcessor(
      symbolResolver = MarcelSymbolResolver(),
      scriptType = Script::class.javaType,
      cst = SourceFileCstNode(LexToken.DUMMY, LexToken.DUMMY, null, emptyList()),
      fileName = "Test",
      nullSafetyMode = NullSafetyMode.DISABLED
    )
  }
}