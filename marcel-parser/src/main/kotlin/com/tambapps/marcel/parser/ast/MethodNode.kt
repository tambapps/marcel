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

open class MethodNode constructor(override val access: Int, override val ownerClass: JavaType, override val name: String, val block: FunctionBlockNode,
                                  override val parameters: MutableList<MethodParameter>, override val returnType: JavaType, val scope: MethodScope
): AstNode, JavaMethod {

  override val descriptor get() = AsmUtils.getDescriptor(parameters, returnType)
  val parameterTypes get() = parameters.map { it.type.realClassOrObject }.toTypedArray()

  override fun toString(): String {
    return "fun $name(" + parameters.joinToString(separator = ", ") + ") " + returnType
  }

}