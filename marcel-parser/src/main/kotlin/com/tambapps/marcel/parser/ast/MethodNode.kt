package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.ast.expression.FunctionBlockNode
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.owner.Owner
import com.tambapps.marcel.parser.scope.MethodScope
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import org.objectweb.asm.Opcodes

open class MethodNode constructor(val access: Int, val ownerClass: JavaType, val name: String, val block: FunctionBlockNode,
                 val parameters: MutableList<MethodParameter>, val returnType: JavaType, val scope: MethodScope
): AstNode, JavaMethod {

  val invokeCode = Opcodes.INVOKESTATIC

  override val descriptor = AsmUtils.getDescriptor(parameters, returnType)
  override val parameterTypes get() = parameters.map { it.type.realClassOrObject }.toTypedArray()

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