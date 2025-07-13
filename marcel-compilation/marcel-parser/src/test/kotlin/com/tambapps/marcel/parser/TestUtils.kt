package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.IdentifiableCstNode
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.fail

object TestUtils {

  fun parser(text: String) = MarcelParser("Test", MarcelLexer().lex(text))

  fun assertIsEqual(expected: IdentifiableCstNode, actual: CstNode) {
    assertTrue(expected.isEqualTo(actual), "Expected $actual to be equal to $expected")
  }

  fun assertIsEqual(node1: List<IdentifiableCstNode>, node2: List<IdentifiableCstNode>) {
    if (node1.size != node2.size) {
      fail("Expected a list of size ${node1.size} but got ${node2.size}")
    }
    for (i in node1.indices) {
      assertIsEqual(node1[i], node2[i])
    }
  }

  fun assertIsNotEqual(node1: IdentifiableCstNode, node2: CstNode) {
    assertFalse(node1.isEqualTo(node2), "Expected $node1 not to be equal to $node2")
  }
}