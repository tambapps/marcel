package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.type.JavaType

class ClassNode(val access: Int, val name: String, val parentType: JavaType, val methods: MutableList<MethodNode>): AstNode {
  fun addMethod(method: MethodNode) {
    if (methods.any { it.name == method.name }) {
      // TODO handle overloading
      throw SemanticException("Cannot have two methods with the same name")
    }
    methods.add(method)
  }

  override fun toString(): String {
    return "class $name {\n" + methods.joinToString(separator = "\n", transform = { "  $it" }) + "\n}"
  }
}