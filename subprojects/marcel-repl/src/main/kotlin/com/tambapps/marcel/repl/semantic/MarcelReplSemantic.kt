package com.tambapps.marcel.repl.semantic

import com.tambapps.marcel.parser.cst.SourceFileNode
import com.tambapps.marcel.parser.cst.expression.BinaryOperatorNode
import com.tambapps.marcel.parser.cst.expression.reference.ReferenceNode
import com.tambapps.marcel.repl.ReplJavaTypeResolver
import com.tambapps.marcel.semantic.MarcelSemantic
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.ThisReferenceNode
import com.tambapps.marcel.semantic.variable.field.BoundField

class MarcelReplSemantic(private val replTypeResolver: ReplJavaTypeResolver, cst: SourceFileNode) : MarcelSemantic(replTypeResolver, cst) {

  override fun assignment(node: BinaryOperatorNode): ExpressionNode {
    val scope = currentMethodScope
    if (scope.isStatic || !scope.classType.isScript || node.leftOperand !is com.tambapps.marcel.parser.cst.expression.reference.ReferenceNode) return super.assignment(node)
    val leftResult = runCatching { node.leftOperand.accept(this) }
    if (leftResult.isSuccess) return assignment(node, leftResult.getOrThrow())

    // if we went here this means the field was not defined
    val right = node.rightOperand.accept(this)

    // this is important. We always want bound field to be object type as values are obtained from getVariable which returns an Object
    val boundField = BoundField(right.type.objectType, (node.leftOperand as com.tambapps.marcel.parser.cst.expression.reference.ReferenceNode).value, scope.classType)
    replTypeResolver.defineBoundField(boundField)

    return assignment(node, left = ReferenceNode(
      owner = ThisReferenceNode(currentScope.classType, node.token),
      variable = boundField,
      token = node.token
    ), right = caster.cast(boundField.type, right))
  }
}