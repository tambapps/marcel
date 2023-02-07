package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.ast.expression.FunctionBlockNode
import com.tambapps.marcel.parser.scope.MethodScope
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType

open class MethodNode constructor(override val access: Int, override val ownerClass: JavaType, override val name: String, val block: FunctionBlockNode,
                                  override val parameters: MutableList<MethodParameter>, override val returnType: JavaType, val scope: MethodScope,
                                  override val isInline: Boolean
): AstNode, JavaMethod {

  override val isConstructor = false
  override val isAbstract = false
  override val isDefault = false

  override val descriptor get() = AsmUtils.getDescriptor(parameters, returnType)

  override fun toString(): String {
    return "fun $name(" + parameters.joinToString(separator = ", ") + ") " + returnType
  }

  override fun accept(visitor: AstVisitor) {
    super.accept(visitor)
    block.accept(visitor)
  }
}