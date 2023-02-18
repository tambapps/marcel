package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.scope.ClassField
import com.tambapps.marcel.parser.type.JavaType

class FieldNode constructor(type: JavaType, name: String, owner: JavaType, access: Int, val initialValue: ExpressionNode?) : AstNode,
  ClassField(type, name, owner, access) {


  override fun toString(): String {
    var s =  "${type.simpleName} $name"
    if (initialValue != null) s += " $initialValue"
    return s
  }
}