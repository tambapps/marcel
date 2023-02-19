package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.ast.expression.FunctionBlockNode
import com.tambapps.marcel.parser.ast.expression.FunctionCallNode
import com.tambapps.marcel.parser.ast.expression.ReferenceExpression
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.scope.MethodScope
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import org.objectweb.asm.Opcodes

open class MethodNode constructor(override val access: Int, final override val ownerClass: JavaType, override val name: String, val block: FunctionBlockNode,
                                  final override val parameters: MutableList<MethodParameter>, final override val returnType: JavaType, val scope: MethodScope,
                                  override val isInline: Boolean,
                                  final override val isConstructor: Boolean
): AstNode, JavaMethod {

  override val actualReturnType = returnType
  constructor(access: Int, ownerClass: JavaType, name: String, block: FunctionBlockNode,
              parameters: MutableList<MethodParameter>, returnType: JavaType, scope: MethodScope,
              isInline: Boolean): this(access, ownerClass, name, block, parameters, returnType, scope, isInline, false)

  companion object {
    fun fromJavaMethod(classScope: Scope, javaMethod: JavaMethod): MethodNode {
      val methodScope = MethodScope(classScope, javaMethod.name, javaMethod.parameters, javaMethod.returnType)
      return MethodNode(Opcodes.ACC_PUBLIC, javaMethod.ownerClass, javaMethod.name, FunctionBlockNode(methodScope,
          mutableListOf()
      ), methodScope.parameters.toMutableList(), javaMethod.returnType, methodScope, false)
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