package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType

class ClassNode constructor(val scope: Scope, val access: Int, val type: JavaType, val superType: JavaType,
                            val isScript: Boolean,
                            val methods: MutableList<MethodNode>,
                            val fields: List<FieldNode>,
                            val innerClasses: MutableList<ClassNode>): AstNode {

  val constructorsCount: Int
    get() = methods.count { it.name == JavaMethod.CONSTRUCTOR_NAME }
  val constructors: List<ConstructorNode>
    get() = methods.mapNotNull { it as? ConstructorNode }

  val internalName = AsmUtils.getInternalName(type)
  fun addMethod(method: MethodNode) {
    if (methods.any { it.matches(scope.typeResolver, method.name, method.parameters) }) {
      throw SemanticException("Cannot have two methods with the same name")
    }
    methods.add(method)
  }

  override fun toString(): String {
    return "class $type {\n" + methods.joinToString(separator = "\n", transform = { "  $it" }) + "\n}"
  }
}