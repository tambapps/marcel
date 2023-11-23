package com.tambapps.marcel.repl.semantic

import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.parser.cst.expression.BinaryOperatorCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ReferenceCstNode
import com.tambapps.marcel.repl.ReplJavaTypeResolver
import com.tambapps.marcel.semantic.MarcelSemantic
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.ThisReferenceNode
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import com.tambapps.marcel.semantic.variable.field.BoundField

class MarcelReplSemantic(private val replTypeResolver: ReplJavaTypeResolver, cst: SourceFileCstNode) : MarcelSemantic(replTypeResolver, cst) {

  override fun assignment(node: BinaryOperatorCstNode): ExpressionNode {
    val scope = currentMethodScope
    if (scope.isStatic || !scope.classType.isScript || node.leftOperand !is ReferenceCstNode) return super.assignment(node)
    val leftResult = runCatching { node.leftOperand.accept(this) }
    if (leftResult.isSuccess) return assignment(node, leftResult.getOrThrow())

    // if we went here this means the field was not defined
    val right = node.rightOperand.accept(this)

    val boundField = BoundField(right.type.objectType, (node.leftOperand as ReferenceCstNode).value, scope.classType)
    replTypeResolver.defineBoundField(boundField)

    return assignment(node, left = ReferenceNode(
      owner = ThisReferenceNode(currentScope.classType, node.token),
      variable = boundField,
      token = node.token
    ), right = caster.cast(boundField.type, right))
  }
}