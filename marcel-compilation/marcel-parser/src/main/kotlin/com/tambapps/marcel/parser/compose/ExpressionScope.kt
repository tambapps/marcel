package com.tambapps.marcel.parser.compose

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.cst.AccessCstNode
import com.tambapps.marcel.parser.cst.CstNode
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
import com.tambapps.marcel.parser.cst.expression.literal.CharCstNode
import com.tambapps.marcel.parser.cst.expression.literal.DoubleCstNode
import com.tambapps.marcel.parser.cst.expression.literal.FloatCstNode
import com.tambapps.marcel.parser.cst.expression.literal.IntCstNode
import com.tambapps.marcel.parser.cst.expression.literal.LongCstNode
import com.tambapps.marcel.parser.cst.expression.literal.MapCstNode
import com.tambapps.marcel.parser.cst.expression.literal.NullCstNode
import com.tambapps.marcel.parser.cst.expression.literal.RegexCstNode
import com.tambapps.marcel.parser.cst.expression.literal.StringCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ClassReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.DirectFieldReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.IncrCstNode
import com.tambapps.marcel.parser.cst.expression.reference.IndexAccessCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.SuperReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ThisReferenceCstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNode
import com.tambapps.marcel.parser.cst.statement.VariableDeclarationCstNode

/**
 * Scope of an expression composition
 */
open class ExpressionScope(
  val tokenStart: LexToken = LexToken.DUMMY,
  val tokenEnd: LexToken = LexToken.DUMMY,
  val parent: CstNode? = null
) {

  fun fCall(value: String, castType: TypeCstNode? = null, args: List<ExpressionCstNode> = emptyList(),
            namedArgs: List<Pair<String, ExpressionCstNode>> = emptyList()
  ) = FunctionCallCstNode(parent = parent, value = value, castType = castType,
    positionalArgumentNodes = args, namedArgumentNodes = namedArgs,
    tokenStart = tokenStart, tokenEnd = tokenEnd
  )

  fun truthyVarDecl(type: TypeCstNode, name: String, expr: ExpressionCstNode) = TruthyVariableDeclarationCstNode(parent, tokenStart, tokenEnd, type, identifierToken(name), expr)

  fun map(vararg pairs: Pair<ExpressionCstNode, ExpressionCstNode>) = MapCstNode(
    pairs.toList(),
    parent = parent,
    tokenStart = tokenStart,
    tokenEnd = tokenEnd
  )
  fun array(vararg expr: ExpressionCstNode) = ArrayCstNode(expr.toList(), parent = parent, tokenStart = tokenStart, tokenEnd = tokenEnd)
  fun minus(expr: ExpressionCstNode) = UnaryMinusCstNode(expr, parent, tokenStart, tokenEnd)
  fun not(expr: ExpressionCstNode) = NotCstNode(expr, parent, tokenStart, tokenEnd)
  fun incr(varName: String, returnValueBefore: Boolean, amount: Int = 1) = IncrCstNode( parent, varName, amount, returnValueBefore,tokenStart, tokenEnd)

  inline fun async(compose: StatementScope.() -> Unit): AsyncBlockCstNode {
    val stmtComposer = BlockStatementScope(tokenStart = tokenStart, tokenEnd = tokenEnd, parent = parent)
    compose.invoke(stmtComposer)
    return AsyncBlockCstNode(parent, tokenStart, tokenEnd, stmtComposer.asBlock())
  }

  fun allIn(
    varType: TypeCstNode,
    varName: String,
    filterExpr: ExpressionCstNode,
    inExpr: ExpressionCstNode? = null,
    negate: Boolean = false) = AllInCstNode(parent, tokenStart, tokenEnd, varType, varName, inExpr, filterExpr, negate)

  fun anyIn(
    varType: TypeCstNode,
    varName: String,
    filterExpr: ExpressionCstNode,
    inExpr: ExpressionCstNode? = null,
    negate: Boolean = false) = AnyInCstNode(parent, tokenStart, tokenEnd, varType, varName, inExpr, filterExpr, negate)

  fun findIn(
    varType: TypeCstNode,
    varName: String,
    filterExpr: ExpressionCstNode,
    inExpr: ExpressionCstNode? = null
  ) = FindInCstNode(parent, tokenStart, tokenEnd, varType, varName, inExpr, filterExpr)

  fun mapFilter(
    varType: TypeCstNode,
    varName: String,
    mapExpr: ExpressionCstNode,
    filterExpr: ExpressionCstNode? = null,
    inExpr: ExpressionCstNode? = null
  ) = MapFilterCstNode(parent, tokenStart, tokenEnd, varType, varName, inExpr, mapExpr, filterExpr)

  fun thisRef() = ThisReferenceCstNode(token = tokenStart, parent = parent)
  fun superRef() = SuperReferenceCstNode(token = tokenStart, parent = parent)

  fun new(type: TypeCstNode, args: List<ExpressionCstNode> = emptyList(),
          namedArgs: List<Pair<String, ExpressionCstNode>> = emptyList()) = NewInstanceCstNode(parent, type, args, namedArgs, tokenStart, tokenEnd)
  fun superConstrCall(args: List<ExpressionCstNode> = emptyList(),
                      namedArgs: List<Pair<String, ExpressionCstNode>> = emptyList()) = SuperConstructorCallCstNode(parent, args, namedArgs, tokenStart, tokenEnd)
  fun thisConstrCall(args: List<ExpressionCstNode> = emptyList(),
                      namedArgs: List<Pair<String, ExpressionCstNode>> = emptyList()) = ThisConstructorCallCstNode(parent, args, namedArgs, tokenStart, tokenEnd)

  fun ternary(test: ExpressionCstNode, trueExpr: ExpressionCstNode, falseExpr: ExpressionCstNode) =
    TernaryCstNode(test, trueExpr, falseExpr, parent, tokenStart, tokenEnd)

  fun and(left: ExpressionCstNode, right: ExpressionCstNode) = binaryOperator(TokenType.AND, left, right)
  fun plus(left: ExpressionCstNode, right: ExpressionCstNode) = binaryOperator(TokenType.PLUS, left, right)
  fun minus(left: ExpressionCstNode, right: ExpressionCstNode) = binaryOperator(TokenType.MINUS, left, right)
  fun mul(left: ExpressionCstNode, right: ExpressionCstNode) = binaryOperator(TokenType.MUL, left, right)
  fun dot(left: ExpressionCstNode, right: ExpressionCstNode) = binaryOperator(TokenType.DOT, left, right)
  fun eq(left: ExpressionCstNode, right: ExpressionCstNode) = binaryOperator(TokenType.EQUAL, left, right)
  fun gt(left: ExpressionCstNode, right: ExpressionCstNode) = binaryOperator(TokenType.GT, left, right)
  fun lt(left: ExpressionCstNode, right: ExpressionCstNode) = binaryOperator(TokenType.LT, left, right)
  fun goe(left: ExpressionCstNode, right: ExpressionCstNode) = binaryOperator(TokenType.GOE, left, right)
  fun loe(left: ExpressionCstNode, right: ExpressionCstNode) = binaryOperator(TokenType.LOE, left, right)
  private fun binaryOperator(type: TokenType, left: ExpressionCstNode, right: ExpressionCstNode) =
    BinaryOperatorCstNode(type, left, right, parent, tokenStart, tokenEnd)

  fun elvisThrow(expr: ExpressionCstNode, throwable: ExpressionCstNode) = ElvisThrowCstNode(parent, tokenStart, tokenEnd, expr, throwable)

  fun asType(left: ExpressionCstNode, right: TypeCstNode) = binaryOTypeOperator(TokenType.AS, left, right)
  fun instanceof(left: ExpressionCstNode, right: TypeCstNode) = binaryOTypeOperator(TokenType.INSTANCEOF, left, right)
  fun notInstanceof(left: ExpressionCstNode, right: TypeCstNode) = binaryOTypeOperator(TokenType.NOT_INSTANCEOF, left, right)
  private fun binaryOTypeOperator(type: TokenType, left: ExpressionCstNode, right: TypeCstNode) =
    BinaryTypeOperatorCstNode(type, left, right, parent, tokenStart, tokenEnd)

  fun indexAccess(owner: ExpressionCstNode, indexes: List<ExpressionCstNode>, isSafeAccess: Boolean = false) =
    IndexAccessCstNode(parent, owner, indexes, isSafeAccess, tokenStart, tokenEnd)
  fun nullValue() = NullCstNode(token = tokenStart)
  fun type(value: String, genericTypes: List<TypeCstNode> = emptyList(), arrayDimensions: Int = 0) = TypeCstNode(parent, value, genericTypes, arrayDimensions, tokenStart, tokenEnd)
  fun int(value: Int) = IntCstNode(value = value, token = tokenStart)
  fun bool(value: Boolean) = BoolCstNode(value = value, token = tokenStart)
  fun string(value: Any) = StringCstNode(value = value.toString(), token = tokenStart)
  fun templateSting(value: Any) = TemplateStringCstNode(expressions = listOf(string(value)), tokenStart = tokenStart, tokenEnd = tokenEnd, parent = parent)
  fun float(value: Float) = FloatCstNode(value = value, token = tokenStart)
  fun char(value: Char) = CharCstNode(value = value, tokenStart = tokenStart, tokenEnd = tokenEnd)
  fun long(value: Long) = LongCstNode(value = value, token = tokenStart)
  fun double(value: Double) = DoubleCstNode(value = value, token = tokenStart)
  fun ref(name: String) = ReferenceCstNode(value = name, token = tokenStart, parent = parent)
  fun directFieldRef(name: String) = DirectFieldReferenceCstNode(value = name, token = tokenStart, parent = parent)
  fun classReference(type: TypeCstNode) = ClassReferenceCstNode(parent, type, tokenStart, tokenEnd)
  fun regex(value: String, flags: String = "") = RegexCstNode(
    value = value,
    flags = flags.map { RegexCstNode.FLAGS_MAP.getValue(it) },
    tokenStart = tokenStart,
    tokenEnd = tokenEnd)

  inline fun whenExpr(branchesGenerator: WhenScope.() -> Unit): WhenCstNode {
    val whenScope = WhenScope()
    branchesGenerator.invoke(whenScope)
    return WhenCstNode(parent, tokenStart, tokenEnd, whenScope.branches, whenScope.elseBranch)
  }

  inline fun switchExpr(switchExpr: ExpressionCstNode, branchesGenerator: WhenScope.() -> Unit): WhenCstNode {
    val whenScope = WhenScope()
    branchesGenerator.invoke(whenScope)
    return SwitchCstNode(parent, tokenStart, tokenEnd, whenScope.branches, whenScope.elseBranch, null, switchExpr)
  }

  inline fun switchExpr(varType: TypeCstNode, isVarNullable: Boolean, varName: String, switchExpr: ExpressionCstNode, branchesGenerator: WhenScope.() -> Unit): WhenCstNode {
    val whenScope = WhenScope()
    branchesGenerator.invoke(whenScope)
    return SwitchCstNode(parent, tokenStart, tokenEnd, whenScope.branches, whenScope.elseBranch,
      VariableDeclarationCstNode(
        varType,
        identifierToken(varName),
        null,
        isVarNullable,
        parent,
        tokenStart
        , tokenEnd),
      switchExpr)
  }

  fun access(isStatic: Boolean = false, isFinal: Boolean = false, isExplicit: Boolean = true,
                       visibility: TokenType = TokenType.VISIBILITY_PUBLIC
  ) = AccessCstNode(
      parent = parent,
      tokenStart = LexToken.DUMMY,
      tokenEnd = LexToken.DUMMY,
      isStatic = isStatic,
      isFinal = isFinal,
      isInline = false,
      visibility = visibility,
      isExplicit = isExplicit
    )

}

class WhenScope: ExpressionScope() {
  val branches = mutableListOf<Pair<ExpressionCstNode, StatementCstNode>>()
  var elseBranch: StatementCstNode? = null

  inline fun branch(expr: ExpressionCstNode, compose: StatementScope.() -> StatementCstNode) {
    val stmtComposer = StatementScope(tokenStart = tokenStart, tokenEnd = tokenEnd)
    branches.add(expr to compose.invoke(stmtComposer))
  }

  inline fun elseBranch(compose: StatementScope.() -> StatementCstNode) {
    val stmtComposer = StatementScope(tokenStart = tokenStart, tokenEnd = tokenEnd)
    elseBranch = compose.invoke(stmtComposer)
  }
}

fun identifierToken(name: String): LexToken {
  return LexToken(0, 0, 0, 0, TokenType.IDENTIFIER, name)
}
