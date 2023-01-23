package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaType

class ClassNode(val scope: Scope, val access: Int, val type: JavaType, val parentType: JavaType,
                val methods: MutableList<MethodNode>): AstNode {

  val internalName = AsmUtils.getInternalName(type)
  fun addMethod(method: MethodNode) {
    if (methods.any { it.matches(method.name, method.parameters) }) {
      throw SemanticException("Cannot have two methods with the same name")
    }
    methods.add(method)
  }

  override fun toString(): String {
    return "class $type {\n" + methods.joinToString(separator = "\n", transform = { "  $it" }) + "\n}"
  }
}