package com.tambapps.marcel.repl.semantic

import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.parser.cst.expression.BinaryOperatorCstNode
import com.tambapps.marcel.parser.cst.expression.FunctionCallCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ReferenceCstNode as ReferenceCstNode
import com.tambapps.marcel.repl.ReplMarcelSymbolResolver
import com.tambapps.marcel.semantic.MarcelSemantic
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.ThisReferenceNode
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.variable.field.BoundField
import marcel.lang.Script

class MarcelReplSemantic(private val replSymbolResolver: ReplMarcelSymbolResolver, cst: SourceFileCstNode, fileName: String) : MarcelSemantic(replSymbolResolver, Script::class.javaType, cst, fileName) {

  override fun assignment(node: BinaryOperatorCstNode, smartCastType: JavaType?): ExpressionNode {
    val scope = currentMethodScope
    if (scope.isStatic || !scope.classType.isScript || node.leftOperand !is ReferenceCstNode) return super.assignment(node, smartCastType)
    val leftResult = runCatching { node.leftOperand.accept(this) }
    if (leftResult.isSuccess) return assignment(node, leftResult.getOrThrow())

    // if we went here this means the field was not defined
    val right = node.rightOperand.accept(this)

    // this is important. We always want bound field to be object type as values are obtained from getVariable which returns an Object
    val boundField = BoundField(right.type.objectType, (node.leftOperand as ReferenceCstNode).value, scope.classType)
    replSymbolResolver.defineBoundField(boundField)

    return assignment(node, left = ReferenceNode(
      owner = ThisReferenceNode(currentScope.classType, node.token),
      variable = boundField,
      token = node.token
    ), right = caster.cast(boundField.type, right))
  }

  override fun resolveMethodCall(
    node: FunctionCallCstNode,
    positionalArguments: List<ExpressionNode>,
    namedArguments: List<Pair<String, ExpressionNode>>,
    castType: JavaType?
  ): ExpressionNode? {
    val e = super.resolveMethodCall(node, positionalArguments, namedArguments, castType)
    if (e != null) return e

    // handle delegate function call
    val delegateVariable = getDelegate() ?: return null

    val methodResolve = methodResolver.resolveMethod(node, delegateVariable.type, node.value, positionalArguments, namedArguments)

    if (methodResolve != null && !methodResolve.first.isMarcelStatic) {
      val owner = ReferenceNode(variable = delegateVariable, token = node.token, owner = ThisReferenceNode(currentScope.classType, node.token))
      return fCall(methodResolve = methodResolve, owner = owner, castType = castType,
        tokenStart = node.tokenStart,
        tokenEnd = node.tokenEnd)
    }
    return null
  }

  private fun getDelegate() = (symbolResolver as ReplMarcelSymbolResolver).getBoundField("delegate")
}