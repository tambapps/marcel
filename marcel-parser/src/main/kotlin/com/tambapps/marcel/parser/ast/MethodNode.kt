package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaType

class MethodNode(val access: Int, val name: String, val statements: MutableList<StatementNode>,
                 val parameterTypes: Array<JavaType>, val returnType: JavaType, val scope: Scope): AstNode {

  val methodDescriptor: String
    get() {
      val builder = StringBuilder().append('(')
      parameterTypes.joinTo(builder, separator = "", transform = { it.descriptor })
      return builder.append(')')
        .append(returnType.descriptor)
        .toString()
    }
}