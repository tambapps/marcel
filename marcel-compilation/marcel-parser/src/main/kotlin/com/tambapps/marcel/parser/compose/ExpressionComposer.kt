package com.tambapps.marcel.parser.compose

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.expression.AllInCstNode
import com.tambapps.marcel.parser.cst.expression.AnyInCstNode
import com.tambapps.marcel.parser.cst.expression.AsyncBlockCstNode
import com.tambapps.marcel.parser.cst.expression.BinaryOperatorCstNode
import com.tambapps.marcel.parser.cst.expression.BinaryTypeOperatorCstNode
import com.tambapps.marcel.parser.cst.expression.ElvisThrowCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.expression.FindInCstNode
import com.tambapps.marcel.parser.cst.expression.FunctionCallCstNode
import com.tambapps.marcel.parser.cst.expression.MapFilterCstNode
import com.tambapps.marcel.parser.cst.expression.NewInstanceCstNode
import com.tambapps.marcel.parser.cst.expression.NotCstNode
import com.tambapps.marcel.parser.cst.expression.SuperConstructorCallCstNode
import com.tambapps.marcel.parser.cst.expression.SwitchCstNode
import com.tambapps.marcel.parser.cst.expression.TemplateStringCstNode
import com.tambapps.marcel.parser.cst.expression.TernaryCstNode
import com.tambapps.marcel.parser.cst.expression.ThisConstructorCallCstNode
import com.tambapps.marcel.parser.cst.expression.TruthyVariableDeclarationCstNode
import com.tambapps.marcel.parser.cst.expression.UnaryMinusCstNode
import com.tambapps.marcel.parser.cst.expression.WhenCstNode
import com.tambapps.marcel.parser.cst.expression.literal.ArrayCstNode
import com.tambapps.marcel.parser.cst.expression.literal.BoolCstNode
import com.tambapps.marcel.parser.cst.expression.literal.DoubleCstNode
import com.tambapps.marcel.parser.cst.expression.literal.FloatCstNode
import com.tambapps.marcel.parser.cst.expression.literal.IntCstNode
import com.tambapps.marcel.parser.cst.expression.literal.LongCstNode
import com.tambapps.marcel.parser.cst.expression.literal.NullCstNode
import com.tambapps.marcel.parser.cst.expression.literal.StringCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ClassReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.IndexAccessCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ReferenceCstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNode
import com.tambapps.marcel.parser.cst.statement.VariableDeclarationCstNode

open class ExpressionComposer(
  val tokenStart: LexToken = LexToken.DUMMY,
  val tokenEnd: LexToken = LexToken.DUMMY,
) {

  companion object {
    fun compose(composer: ExpressionComposer.() -> ExpressionCstNode) = composer.invoke(ExpressionComposer())
  }

  fun fCall(value: String, castType: TypeCstNode? = null, args: List<ExpressionCstNode> = emptyList(),
            namedArgs: List<Pair<String, ExpressionCstNode>> = emptyList()
  ) = FunctionCallCstNode(parent = null, value = value, castType = castType,
    positionalArgumentNodes = args, namedArgumentNodes = namedArgs,
    tokenStart = tokenStart, tokenEnd = tokenEnd
  )

  fun truthyVarDecl(type: TypeCstNode, name: String, expr: ExpressionCstNode) = TruthyVariableDeclarationCstNode(null, tokenStart, tokenEnd, type, identifierToken(name), expr)

  fun array(vararg expr: ExpressionCstNode) = ArrayCstNode(expr.toList(), parent = null, tokenStart = tokenStart, tokenEnd = tokenEnd)
  fun minus(expr: ExpressionCstNode) = UnaryMinusCstNode(expr, null, tokenStart, tokenEnd)
  fun not(expr: ExpressionCstNode) = NotCstNode(expr, null, tokenStart, tokenEnd)

  fun async(compose: StatementsComposer.() -> Unit): AsyncBlockCstNode {
    val stmtComposer = StatementsComposer(tokenStart = tokenStart, tokenEnd = tokenEnd)
    compose.invoke(stmtComposer)
    return AsyncBlockCstNode(null, tokenStart, tokenEnd, stmtComposer.asBlock())
  }

  fun allIn(
    varType: TypeCstNode,
    varName: String,
    filterExpr: ExpressionCstNode,
    inExpr: ExpressionCstNode? = null,
    negate: Boolean = false) = AllInCstNode(null, tokenStart, tokenEnd, varType, varName, inExpr, filterExpr, negate)

  fun anyIn(
    varType: TypeCstNode,
    varName: String,
    filterExpr: ExpressionCstNode,
    inExpr: ExpressionCstNode? = null,
    negate: Boolean = false) = AnyInCstNode(null, tokenStart, tokenEnd, varType, varName, inExpr, filterExpr, negate)

  fun findIn(
    varType: TypeCstNode,
    varName: String,
    filterExpr: ExpressionCstNode,
    inExpr: ExpressionCstNode? = null
  ) = FindInCstNode(null, tokenStart, tokenEnd, varType, varName, inExpr, filterExpr)

  fun mapFilter(
    varType: TypeCstNode,
    varName: String,
    mapExpr: ExpressionCstNode,
    filterExpr: ExpressionCstNode? = null,
    inExpr: ExpressionCstNode? = null
  ) = MapFilterCstNode(null, tokenStart, tokenEnd, varType, varName, inExpr, mapExpr, filterExpr)

  fun new(type: TypeCstNode, args: List<ExpressionCstNode> = emptyList(),
          namedArgs: List<Pair<String, ExpressionCstNode>> = emptyList()) = NewInstanceCstNode(null, type, args, namedArgs, tokenStart, tokenEnd)
  fun superConstrCall(args: List<ExpressionCstNode> = emptyList(),
                      namedArgs: List<Pair<String, ExpressionCstNode>> = emptyList()) = SuperConstructorCallCstNode(null, args, namedArgs, tokenStart, tokenEnd)
  fun thisConstrCall(args: List<ExpressionCstNode> = emptyList(),
                      namedArgs: List<Pair<String, ExpressionCstNode>> = emptyList()) = ThisConstructorCallCstNode(null, args, namedArgs, tokenStart, tokenEnd)

  fun ternary(test: ExpressionCstNode, trueExpr: ExpressionCstNode, falseExpr: ExpressionCstNode) =
    TernaryCstNode(test, trueExpr, falseExpr, null, tokenStart, tokenEnd)

  fun and(left: ExpressionCstNode, right: ExpressionCstNode) = binaryOperator(TokenType.AND, left, right)
  fun plus(left: ExpressionCstNode, right: ExpressionCstNode) = binaryOperator(TokenType.PLUS, left, right)
  fun mul(left: ExpressionCstNode, right: ExpressionCstNode) = binaryOperator(TokenType.MUL, left, right)
  fun dot(left: ExpressionCstNode, right: ExpressionCstNode) = binaryOperator(TokenType.DOT, left, right)
  fun eq(left: ExpressionCstNode, right: ExpressionCstNode) = binaryOperator(TokenType.EQUAL, left, right)
  fun gt(left: ExpressionCstNode, right: ExpressionCstNode) = binaryOperator(TokenType.GT, left, right)
  fun lt(left: ExpressionCstNode, right: ExpressionCstNode) = binaryOperator(TokenType.LT, left, right)
  fun goe(left: ExpressionCstNode, right: ExpressionCstNode) = binaryOperator(TokenType.GOE, left, right)
  fun loe(left: ExpressionCstNode, right: ExpressionCstNode) = binaryOperator(TokenType.LOE, left, right)
  private fun binaryOperator(type: TokenType, left: ExpressionCstNode, right: ExpressionCstNode) =
    BinaryOperatorCstNode(type, left, right, null, tokenStart, tokenEnd)

  fun elvisThrow(expr: ExpressionCstNode, throwable: ExpressionCstNode) = ElvisThrowCstNode(null, tokenStart, tokenEnd, expr, throwable)

  fun asType(left: ExpressionCstNode, right: TypeCstNode) = binaryOTypeOperator(TokenType.AS, left, right)
  fun instanceof(left: ExpressionCstNode, right: TypeCstNode) = binaryOTypeOperator(TokenType.INSTANCEOF, left, right)
  fun notInstanceof(left: ExpressionCstNode, right: TypeCstNode) = binaryOTypeOperator(TokenType.NOT_INSTANCEOF, left, right)
  private fun binaryOTypeOperator(type: TokenType, left: ExpressionCstNode, right: TypeCstNode) =
    BinaryTypeOperatorCstNode(type, left, right, null, tokenStart, tokenEnd)

  fun indexAccess(owner: ExpressionCstNode, indexes: List<ExpressionCstNode>, isSafeAccess: Boolean = false) =
    IndexAccessCstNode(null, owner, indexes, isSafeAccess, tokenStart, tokenEnd)
  fun nullValue() = NullCstNode(token = tokenStart)
  fun type(value: String, genericTypes: List<TypeCstNode> = emptyList(), arrayDimensions: Int = 0) = TypeCstNode(null, value, genericTypes, arrayDimensions, tokenStart, tokenEnd)
  fun int(value: Int) = IntCstNode(value = value, token = tokenStart)
  fun bool(value: Boolean) = BoolCstNode(value = value, token = tokenStart)
  fun string(value: Any) = StringCstNode(value = value.toString(), token = tokenStart)
  fun templateSting(value: Any) = TemplateStringCstNode(expressions = listOf(string(value)), tokenStart = tokenStart, tokenEnd = tokenEnd, parent = null)
  fun float(value: Float) = FloatCstNode(value = value, token = tokenStart)
  fun long(value: Long) = LongCstNode(value = value, token = tokenStart)
  fun double(value: Double) = DoubleCstNode(value = value, token = tokenStart)
  fun ref(name: String) = ReferenceCstNode(value = name, token = tokenStart, parent = null)
  fun classReference(type: TypeCstNode) = ClassReferenceCstNode(null, type, tokenStart, tokenEnd)

  fun whenExpr(branchesGenerator: WhenScope.() -> Unit): WhenCstNode {
    val whenScope = WhenScope()
    branchesGenerator.invoke(whenScope)
    return WhenCstNode(null, tokenStart, tokenEnd, whenScope.branches, whenScope.elseBranch)
  }

  fun switchExpr(switchExpr: ExpressionCstNode, branchesGenerator: WhenScope.() -> Unit): WhenCstNode {
    val whenScope = WhenScope()
    branchesGenerator.invoke(whenScope)
    return SwitchCstNode(null, tokenStart, tokenEnd, whenScope.branches, whenScope.elseBranch, null, switchExpr)
  }

  fun switchExpr(varType: TypeCstNode, isVarNullable: Boolean, varName: String, switchExpr: ExpressionCstNode, branchesGenerator: WhenScope.() -> Unit): WhenCstNode {
    val whenScope = WhenScope()
    branchesGenerator.invoke(whenScope)
    return SwitchCstNode(null, tokenStart, tokenEnd, whenScope.branches, whenScope.elseBranch,
      VariableDeclarationCstNode(
        varType,
        identifierToken(varName),
        null,
        isVarNullable,
        null,
        tokenStart
        , tokenEnd),
      switchExpr)
  }

  private fun identifierToken(name: String): LexToken {
    return LexToken(0, 0, 0, 0, TokenType.IDENTIFIER, name)
  }
}

class WhenScope: ExpressionComposer() {
  val branches = mutableListOf<Pair<ExpressionCstNode, StatementCstNode>>()
  var elseBranch: StatementCstNode? = null

  fun branch(expr: ExpressionCstNode, compose: StatementsComposer.() -> Unit) {
    val stmtComposer = StatementsComposer(tokenStart = tokenStart, tokenEnd = tokenEnd)
    compose.invoke(stmtComposer)
    branches.add(expr to stmtComposer.asStmt())
  }

  fun elseBranch(compose: StatementsComposer.() -> Unit) {
    val stmtComposer = StatementsComposer(tokenStart = tokenStart, tokenEnd = tokenEnd)
    compose.invoke(stmtComposer)
    elseBranch = stmtComposer.asStmt()
  }
}