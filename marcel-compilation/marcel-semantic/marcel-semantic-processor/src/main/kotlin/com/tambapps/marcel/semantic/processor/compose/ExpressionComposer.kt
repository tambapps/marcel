package com.tambapps.marcel.semantic.processor.compose

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.InstanceOfNode
import com.tambapps.marcel.semantic.ast.expression.NewInstanceNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.StringNode
import com.tambapps.marcel.semantic.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.semantic.ast.expression.SuperReferenceNode
import com.tambapps.marcel.semantic.ast.expression.ThisReferenceNode
import com.tambapps.marcel.semantic.ast.expression.literal.ArrayNode
import com.tambapps.marcel.semantic.ast.expression.literal.BoolConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.IntConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.StringConstantNode
import com.tambapps.marcel.semantic.ast.expression.operator.IsEqualNode
import com.tambapps.marcel.semantic.ast.expression.operator.IsNotEqualNode
import com.tambapps.marcel.semantic.ast.expression.operator.MinusNode
import com.tambapps.marcel.semantic.ast.expression.operator.MulNode
import com.tambapps.marcel.semantic.ast.expression.operator.NotNode
import com.tambapps.marcel.semantic.ast.expression.operator.PlusNode
import com.tambapps.marcel.semantic.ast.expression.operator.VariableAssignmentNode
import com.tambapps.marcel.semantic.processor.AbstractMarcelSemantic
import com.tambapps.marcel.semantic.processor.cast.ExpressionCaster
import com.tambapps.marcel.semantic.processor.imprt.ImportResolver
import com.tambapps.marcel.semantic.processor.scope.ImportScope
import com.tambapps.marcel.semantic.processor.scope.LocalVariablePool
import com.tambapps.marcel.semantic.processor.scope.MethodScope
import com.tambapps.marcel.semantic.processor.scope.Scope
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.symbol.Visibility
import com.tambapps.marcel.semantic.symbol.method.MarcelMethod
import com.tambapps.marcel.semantic.symbol.method.MarcelMethodImpl
import com.tambapps.marcel.semantic.symbol.method.MethodParameter
import com.tambapps.marcel.semantic.symbol.type.JavaArrayType
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.NullSafetyMode
import com.tambapps.marcel.semantic.symbol.type.Nullness
import com.tambapps.marcel.semantic.symbol.variable.LocalVariable
import com.tambapps.marcel.semantic.symbol.variable.Variable
import com.tambapps.marcel.semantic.symbol.variable.field.MarcelField
import java.util.LinkedList

open class ExpressionComposer constructor(
  scopeQueue: LinkedList<Scope>,
  val caster: ExpressionCaster,
  nullSafetyMode: NullSafetyMode,
  override val symbolResolver: MarcelSymbolResolver,
  val tokenStart: LexToken,
  val tokenEnd: LexToken,
): AbstractMarcelSemantic(scopeQueue, nullSafetyMode), ExpressionCaster by caster {

  constructor(
    methodName: String = "foo",
    methodReturnType: JavaType = JavaType.Object,
    classType: JavaType = JavaType.Object,
    caster: ExpressionCaster,
    symbolResolver: MarcelSymbolResolver = MarcelSymbolResolver(),
    importResolver: ImportResolver = ImportResolver.DEFAULT_IMPORTS,
    staticContext: Boolean = false,
    nullSafetyMode: NullSafetyMode = NullSafetyMode.DISABLED,
    tokenStart: LexToken = LexToken.DUMMY,
    tokenEnd: LexToken = LexToken.DUMMY
  ): this(
    scopeQueue = LinkedList<Scope>().apply {
      add(MethodScope(ImportScope(symbolResolver, importResolver, null),
        MarcelMethodImpl(classType, Visibility.PUBLIC, methodName, Nullness.NULLABLE, emptyList(), methodReturnType), symbolResolver,
        classType,
        importResolver, staticContext = staticContext,
        LocalVariablePool(staticContext), isAsync = false
      ))
    },
    caster = caster,
    nullSafetyMode = nullSafetyMode,
    symbolResolver = symbolResolver,
    tokenStart = tokenStart,
    tokenEnd = tokenEnd
  )

  fun superRef() = SuperReferenceNode(currentScope.classType.superType!!, tokenStart)

  fun ref(
    field: MarcelField,
    owner: ExpressionNode? = if (field.isMarcelStatic) null else ThisReferenceNode(
      field.owner,
      tokenStart
    )
  ): ReferenceNode {
    return ReferenceNode(
      owner = owner,
      variable = field,
      token = tokenStart
    )
  }

  // 0 is outer, 1 is outer of outer, and so on...
  fun outerRef(level: Int = 0) = ref(currentMethodScope.findField("this$$level")!!)

  fun cast(
    expr: ExpressionNode,
    type: JavaType
  ): ExpressionNode = cast(type, expr)

  fun ref(methodParameter: MethodParameter) = ReferenceNode(
    owner = null,
    variable = currentMethodScope.findLocalVariable(methodParameter.name)!!, token = tokenStart
  )

  fun ref(lv: LocalVariable) = ReferenceNode(owner = null, variable = lv, token = tokenStart)

  fun string(value: String) = StringConstantNode(value, tokenStart, tokenEnd)
  fun string(parts: List<ExpressionNode>) =
    StringNode(parts, tokenStart, tokenEnd)

  fun array(
    asType: JavaArrayType,
    vararg elements: ExpressionNode
  ): ArrayNode {
    return array(asType, elements.toMutableList())
  }

  fun array(
    arrayType: JavaArrayType,
    elements: List<ExpressionNode>
  ): ArrayNode {
    return ArrayNode(
      if (elements is MutableList) elements else elements.toMutableList(),
      tokenStart, tokenEnd, arrayType
    )
  }

  fun fCall(
    name: String, arguments: List<ExpressionNode>,
    owner: ExpressionNode,
    castType: JavaType? = null
  ): ExpressionNode {
    val method = symbolResolver.findMethodOrThrow(owner.type, name, arguments, tokenStart)
    return fCall(tokenStart, tokenEnd, method, arguments, owner, castType)
  }

  fun fCall(
    method: MarcelMethod, arguments: List<ExpressionNode>,
    owner: ExpressionNode,
    castType: JavaType? = null
  ): ExpressionNode {
    return fCall(tokenStart, tokenEnd, method, arguments, owner, castType)
  }

  fun fCall(
    name: String, arguments: List<ExpressionNode>,
    ownerType: JavaType,
    castType: JavaType? = null
  ): ExpressionNode {
    val method = symbolResolver.findMethodOrThrow(ownerType, name, arguments, tokenStart)
    return fCall(tokenStart, tokenEnd, method, arguments, null, castType)
  }

  fun superConstructorCall(method: MarcelMethod, arguments: List<ExpressionNode>) = SuperConstructorCallNode(
    method.ownerClass,
    method,
    arguments,
    tokenStart,
    tokenEnd
  )

  fun constructorCall(
    method: MarcelMethod,
    arguments: List<ExpressionNode>
  ): NewInstanceNode {
    return NewInstanceNode(method.ownerClass, method, castedArguments(method, arguments), tokenStart)
  }

  fun thisRef() = ThisReferenceNode(currentScope.classType, tokenStart)

  fun bool(b: Boolean) = BoolConstantNode(tokenStart, b)
  fun int(i: Int) = IntConstantNode(tokenStart, i)

  fun argRef(i: Int) =
    ref(currentMethodScope.findLocalVariable(currentMethodScope.method.parameters[i].name)!!)

  fun lvRef(name: String) = ref(currentMethodScope.findLocalVariable(name)!!)

  fun notExpr(expr: ExpressionNode) = NotNode(expr)
  fun isEqualExpr(
    op1: ExpressionNode,
    op2: ExpressionNode
  ) = IsEqualNode(op1, op2)

  fun isNotEqualExpr(
    op1: ExpressionNode,
    op2: ExpressionNode
  ) = IsNotEqualNode(op1, op2)

  fun isInstanceExpr(type: JavaType, op2: ExpressionNode) =
    InstanceOfNode(type, op2, tokenStart, tokenEnd)

  fun varAssignExpr(
    variable: Variable,
    expr: ExpressionNode,
    owner: ExpressionNode? = null
  ): ExpressionNode {
    return VariableAssignmentNode(
      variable = variable,
      expression = cast(variable.type, expr),
      owner = owner,
      tokenStart = tokenStart,
      tokenEnd = tokenEnd
    )
  }

  fun plus(
    e1: ExpressionNode,
    e2: ExpressionNode
  ): ExpressionNode {
    val commonType = JavaType.commonType(e1, e2)
    return PlusNode(cast(commonType, e1), cast(commonType, e2))
  }

  fun minus(
    e1: ExpressionNode,
    e2: ExpressionNode
  ): ExpressionNode {
    val commonType = JavaType.commonType(e1, e2)
    return MinusNode(cast(commonType, e1), cast(commonType, e2))
  }

  fun mul(
    e1: ExpressionNode,
    e2: ExpressionNode
  ): ExpressionNode {
    val commonType = JavaType.commonType(e1, e2)
    return MulNode(cast(commonType, e1), cast(commonType, e2))
  }
}