package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.ast.expression.FunctionBlockNode
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.owner.Owner
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaType

open class MethodNode(val access: Int, val owner: Owner, val name: String, val block: FunctionBlockNode,
                 val parameters: MutableList<MethodParameter>, val returnType: JavaType, val scope: Scope): AstNode {

  val methodDescriptor = AsmUtils.getDescriptor(parameters.map { it.type }, returnType)

  init {
    for (parameter in parameters) {
      scope.addLocalVariable(parameter.type, parameter.name)
    }
  }
  override fun toString(): String {
    return "fun $name(" + parameters.joinToString(separator = ", ") + ") " + returnType
  }

  fun matches(name: String, types: List<TypedNode>): Boolean {
    if (parameters.size != types.size) return false
    for (i in parameters.indices) {
      if (parameters[i].type != types[i].type) return false
    }
    return this.name == name
  }
}