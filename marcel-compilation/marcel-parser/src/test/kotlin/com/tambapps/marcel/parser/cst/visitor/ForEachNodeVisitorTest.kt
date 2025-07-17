package com.tambapps.marcel.parser.cst.visitor

import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.compose.CstInstructionComposer
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.BinaryOperatorCstNode
import com.tambapps.marcel.parser.cst.expression.TernaryCstNode
import com.tambapps.marcel.parser.cst.expression.literal.DoubleCstNode
import com.tambapps.marcel.parser.cst.expression.literal.FloatCstNode
import com.tambapps.marcel.parser.cst.expression.literal.IntCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ReferenceCstNode
import com.tambapps.marcel.parser.cst.statement.BlockCstNode
import com.tambapps.marcel.parser.cst.statement.ExpressionStatementCstNode
import com.tambapps.marcel.parser.cst.statement.ReturnCstNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ForEachNodeVisitorTest {

  @Test
  fun testExpressionVisitor() {
    val node = generateExpression()
    val list = buildList {
      node.forEach(this::add)
    }

    assertExpressionNodes(list)
  }

  @Test
  fun testStatementVisitor() {
    val node = CstInstructionComposer.block {
      returnStmt()
      stmt(generateExpression())
    }
    val list = buildList {
      node.forEach(this::add)
    }
    assertTrue(list.any { it is BlockCstNode })
    assertTrue(list.any { it is ReturnCstNode })
    assertTrue(list.any { it is ExpressionStatementCstNode })
    assertExpressionNodes(list, otherCount = 3)
  }

  private fun generateExpression() = CstInstructionComposer.expr {
    plus(
      ref("a"),
      minus(
        ternary(
          int(1), float(2f), double(3.0)
        ),
        bool(false)
      )
    )
  }

  private fun assertExpressionNodes(list: List<CstNode>, otherCount: Int = 0) {
    assertTrue(list.any { it is BinaryOperatorCstNode && it.tokenType == TokenType.PLUS })
    assertTrue(list.any { it is ReferenceCstNode && it.value == "a" })
    assertTrue(list.any { it is BinaryOperatorCstNode && it.tokenType == TokenType.MINUS })
    assertTrue(list.any { it is TernaryCstNode })
    assertTrue(list.any { it is IntCstNode && it.value == 1 })
    assertTrue(list.any { it is FloatCstNode && it.value == 2f })
    assertTrue(list.any { it is DoubleCstNode && it.value == 3.0 })
    assertEquals(otherCount + 8, list.size)
  }
}