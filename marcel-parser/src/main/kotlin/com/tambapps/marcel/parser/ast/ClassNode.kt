package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaType

class ClassNode constructor(val scope: Scope, val access: Int, val type: JavaType, val parentType: JavaType,
                val methods: MutableList<MethodNode>,
                val innerClasses: MutableList<ClassNode>): AstNode {

  val internalName = AsmUtils.getInternalName(type)
  fun addMethod(method: MethodNode) {
    if (methods.any { it.matches(method.name, method.parameters) }) {
      throw SemanticException("Cannot have two methods with the same name")
    }
    methods.add(method)
  }

  override fun accept(visitor: AstVisitor) {
    super.accept(visitor)
    methods.forEach { it.accept(visitor) }
  }
  override fun toString(): String {
    return "class $type {\n" + methods.joinToString(separator = "\n", transform = { "  $it" }) + "\n}"
  }
}