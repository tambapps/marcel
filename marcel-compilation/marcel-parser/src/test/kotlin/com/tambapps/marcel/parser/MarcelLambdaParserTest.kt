package com.tambapps.marcel.parser

import com.tambapps.marcel.parser.TestUtils.assertIsEqual
import com.tambapps.marcel.parser.TestUtils.parser
import com.tambapps.marcel.parser.compose.CstExpressionScope
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.expression.FunctionCallCstNode
import com.tambapps.marcel.parser.cst.expression.LambdaCstNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class MarcelLambdaParserTest: CstExpressionScope() {

  @Test
  fun testLambdaArgs() {
    val lambda = parser("{ arg1, Integer arg2 -> }").atom()
    assertTrue(lambda is LambdaCstNode)
    lambda as LambdaCstNode

    assertFalse(lambda.explicit0Parameters)
    assertEquals(2, lambda.parameters.size)
    assertParam(null, "arg1", lambda.parameters[0])
    assertParam(type("Integer"), "arg2", lambda.parameters[1])
  }

  private fun assertParam(type: TypeCstNode?, name: String, param: LambdaCstNode.MethodParameterCstNode) {
    if (type != null) {
      assertNotEquals(null, param.type)
      assertIsEqual(type, param.type!!)
    } else {
      assertEquals(null, param.type)
    }
    assertEquals(name, param.name)
  }

  @Test
  fun testLambdaPrimitiveArgs() {
    val lambda = parser("{ int arg -> }").atom()
    assertTrue(lambda is LambdaCstNode)
    lambda as LambdaCstNode

    assertFalse(lambda.explicit0Parameters)
    assertParam(type("int"), "arg", lambda.parameters[0])
  }

  @ParameterizedTest
  @ValueSource(strings = [
    "assertThrows(ErrorResponseException.class) { ->\n}",
    "assertThrows ErrorResponseException.class \n{ ->\n}" // without parenthesis
  ]) // six numbers
  fun testFunctionCallWithLambdaArg(source: String) {
    val parser = parser(source)
    val fCall = parser.expression(null)
    assertTrue(fCall is FunctionCallCstNode)
    fCall as FunctionCallCstNode
    assertEquals("assertThrows", fCall.value)
    assertNull(fCall.castType)
    assertEquals(2, fCall.positionalArgumentNodes.size)
    assertIsEqual(classReference(type("ErrorResponseException")), fCall.positionalArgumentNodes.first())
    val lambdaArg = fCall.positionalArgumentNodes[1]
    assertTrue(lambdaArg is LambdaCstNode)
    lambdaArg as LambdaCstNode
    assertTrue(lambdaArg.explicit0Parameters)
    assertTrue(lambdaArg.parameters.isEmpty())
    assertTrue(fCall.namedArgumentNodes.isEmpty())
  }


}