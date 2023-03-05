package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.ast.expression.FunctionBlockNode
import com.tambapps.marcel.parser.scope.MethodScope
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.AbstractMethod
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import org.objectweb.asm.Opcodes

open class MethodNode constructor(
  override val token: LexToken, override val access: Int, final override var ownerClass: JavaType, override val name: String, val block: FunctionBlockNode,
  final override val parameters: MutableList<MethodParameterNode>, final override val returnType: JavaType, val scope: MethodScope,
  override val isInline: Boolean,
  final override val isConstructor: Boolean
): AstNode, AbstractMethod() {

  override val actualReturnType = returnType
  constructor(access: Int, ownerClass: JavaType, name: String, block: FunctionBlockNode,
              parameters: MutableList<MethodParameterNode>, returnType: JavaType, scope: MethodScope,
              isInline: Boolean): this(LexToken.dummy(), access, ownerClass, name, block, parameters, returnType, scope, isInline, false)

  companion object {
    fun fromJavaMethod(classScope: Scope, javaMethod: JavaMethod): MethodNode {
      return from(classScope, javaMethod.ownerClass, javaMethod.name, javaMethod.parameters, javaMethod.returnType)
    }

    fun from(classScope: Scope, ownerClass: JavaType, name: String, parameters: List<MethodParameter>, returnType: JavaType): MethodNode {
      val methodScope = MethodScope(classScope,  name, parameters, returnType)
      return MethodNode(Opcodes.ACC_PUBLIC, ownerClass,  name, FunctionBlockNode(
        LexToken.dummy(), methodScope, mutableListOf()
      ), methodScope.parameters.map { MethodParameterNode(it) }.toMutableList(),  returnType, methodScope, false)
    }
  }

  override val isAbstract = false
  override val isDefault = false

  init {
    for (param in parameters) {
      scope.addLocalVariable(param.type, param.name, param.isFinal)
    }

  }

  override val descriptor get() = AsmUtils.getMethodDescriptor(parameters, returnType)

  override fun toString(): String {
    return "fun $name(" + parameters.joinToString(separator = ", ") + ") " + returnType
  }

}