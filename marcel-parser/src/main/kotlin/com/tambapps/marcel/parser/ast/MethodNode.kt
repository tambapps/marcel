package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.owner.Owner
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaType

class MethodNode(val access: Int, val owner: Owner, val name: String, val statements: MutableList<StatementNode>,
                 val parameters: MutableList<MethodParameter>, val returnType: JavaType, val scope: Scope): AstNode {

  val methodDescriptor: String
    get() {
      val builder = StringBuilder().append('(')
      parameters.joinTo(builder, separator = "", transform = { it.type.descriptor })
      return builder.append(')')
        .append(returnType.descriptor)
        .toString()
    }

  override fun toString(): String {
    return "fun $name(" + parameters.joinToString(separator = ", ") + ") " + returnType
  }
}