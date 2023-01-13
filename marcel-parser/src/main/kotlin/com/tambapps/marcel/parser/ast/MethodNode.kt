package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.type.JavaType

class MethodNode(val access: Int, val name: String, val statements: MutableList<StatementNode>,
                 val parameterTypes: Array<JavaType>, val returnType: JavaType): AstNode {

  val methodDescriptor: String
    get() {
      val builder = StringBuilder().append('(')
      parameterTypes.joinTo(builder, separator = "", transform = { it.descriptor })
      return builder.append(')')
        .append(returnType.descriptor)
        .toString()
    }
}