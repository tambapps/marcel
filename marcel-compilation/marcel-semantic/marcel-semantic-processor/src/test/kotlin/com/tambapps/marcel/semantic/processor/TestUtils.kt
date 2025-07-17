package com.tambapps.marcel.semantic.processor

import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.IdentifiableAstNode
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.fail

object TestUtils {

  fun assertIsEqual(expected: IdentifiableAstNode, actual: AstNode) {
    assertTrue(expected.isSemanticEqualTo(actual), "Expected $actual to be equal to $expected")
  }

  fun assertIsEqual(node1: List<IdentifiableAstNode>, node2: List<IdentifiableAstNode>) {
    if (node1.size != node2.size) {
      fail("Expected a list of size ${node1.size} but got ${node2.size}")
    }
    for (i in node1.indices) {
      assertIsEqual(node1[i], node2[i])
    }
  }

  fun assertIsNotEqual(node1: IdentifiableAstNode, node2: AstNode) {
    assertFalse(node1.isSemanticEqualTo(node2), "Expected $node1 not to be equal to $node2")
  }
}