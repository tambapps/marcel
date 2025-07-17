package com.tambapps.marcel.semantic.processor

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.compose.CstInstructionComposer as CstComposer
import com.tambapps.marcel.parser.compose.CstStatementScope
import com.tambapps.marcel.parser.cst.IdentifiableCstNode
import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNode
import com.tambapps.marcel.semantic.ast.IdentifiableAstNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.compose.AstStatementScope
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.processor.TestUtils.assertIsEqual
import com.tambapps.marcel.semantic.processor.TestUtils.assertIsNotEqual
import com.tambapps.marcel.semantic.processor.imprt.ImportResolver
import com.tambapps.marcel.semantic.processor.scope.ClassScope
import com.tambapps.marcel.semantic.processor.scope.MethodScope
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.symbol.Visibility
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.NullSafetyMode
import com.tambapps.marcel.semantic.symbol.type.Nullness
import marcel.lang.Script
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MarcelSemanticTest: AstStatementScope() {

  @Test
  fun testReturn() {
    val processor = processor()
    processor.scopeQueue.push(methodScope(returnType = JavaType.int))

    assertIsEqual(returnStmt(int(123)), processor) { returnStmt(int(123)) }
    assertIsNotEqual(returnStmt(int(124)), processor) { returnStmt(int(123)) }
  }

  @Test
  fun testReturnInvalidType() {
    val processor = processor()
    processor.scopeQueue.push(methodScope(returnType = JavaType.List))

    assertThrows<MarcelSemanticException> {
      CstComposer.stmt { returnStmt(int(123)) }.accept(processor)
      processor.throwIfHasErrors()
    }
  }

  @Test
  fun testConstants() {
    assertIsEqual(int(123)) { int(123) }
    assertIsEqual(float(123.45f)) { float(123.45f) }
    assertIsEqual(long(44355L)) { long(44355L) }
    assertIsEqual(double(1234.657)) { double(1234.657) }
    assertIsEqual(char('c')) { char('c') }
    assertIsEqual(nullValue()) { nullValue() }

    assertIsNotEqual(int(124)) { int(123) }
    assertIsNotEqual(float(123.65f)) { float(123.45f) }
    assertIsNotEqual(long(4435L)) { long(44355L) }
    assertIsNotEqual(double(1234.65)) { double(1234.657) }
    assertIsNotEqual(char('b')) { char('c') }
  }

  fun assertIsEqual(expected: IdentifiableAstNode, processor: SourceFileSemanticProcessor = processor(), cstComposer: CstStatementScope.() -> IdentifiableCstNode) {
    val cst = cstComposer.invoke(CstStatementScope())
    val actual = when (cst) {
      is ExpressionCstNode ->  cst.accept(processor)
      is StatementCstNode ->  cst.accept(processor)
      else -> throw RuntimeException()
    }
    processor.throwIfHasErrors()
    assertIsEqual(expected, actual)
  }

  fun assertIsNotEqual(expected: IdentifiableAstNode, processor: SourceFileSemanticProcessor = processor(), cstComposer: CstStatementScope.() -> IdentifiableCstNode) {
    val cst = cstComposer.invoke(CstStatementScope())
    val actual = when (cst) {
      is ExpressionCstNode ->  cst.accept(processor)
      is StatementCstNode ->  cst.accept(processor)
      else -> throw RuntimeException()
    }
    processor.throwIfHasErrors()
    assertIsNotEqual(expected, actual)
  }

  private fun classScope() = ClassScope(MarcelSymbolResolver(), JavaType.Companion.Object, null, ImportResolver.Companion.DEFAULT_IMPORTS)

  private fun methodScope(returnType: JavaType = JavaType.int) = MethodScope(classScope(), MethodNode(
    "foo",
    Nullness.UNKNOWN,
    mutableListOf(),
    Visibility.PUBLIC,
    returnType,
    isStatic = false,
    LexToken.DUMMY,
    LexToken.DUMMY,
    JavaType.Object
  ))

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