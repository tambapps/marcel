package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.ast.expression.FunctionBlockNode
import com.tambapps.marcel.parser.scope.MethodScope
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.AbstractMethod
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import org.objectweb.asm.Opcodes

open class MethodNode constructor(
  override val token: LexToken, access: Int, final override var ownerClass: JavaType, override val name: String, val block: FunctionBlockNode,
  final override val parameters: MutableList<MethodParameterNode>, final override val returnType: JavaType, val scope: MethodScope,
  override val isInline: Boolean,
  final override val isConstructor: Boolean,
  override val annotations: List<AnnotationNode>
): AstNode, AbstractMethod(access), Annotable {

  override val actualReturnType = returnType
  constructor(token: LexToken, access: Int, ownerClass: JavaType, name: String, block: FunctionBlockNode,
              parameters: MutableList<MethodParameterNode>, returnType: JavaType, scope: MethodScope,
              isInline: Boolean, annotations: List<AnnotationNode>): this(token, access, ownerClass, name, block, parameters, returnType, scope, isInline, false, annotations)

  companion object {
    fun fromJavaMethod(token: LexToken, classScope: Scope, javaMethod: JavaMethod): MethodNode {
      return from(token, classScope, javaMethod.ownerClass, javaMethod.name, javaMethod.parameters, javaMethod.returnType, emptyList(), javaMethod.isStatic)
    }

    fun from(token: LexToken, classScope: Scope, ownerClass: JavaType, name: String, parameters: List<MethodParameter>, returnType: JavaType, annotations: List<AnnotationNode>, staticContext: Boolean): MethodNode {
      val methodScope = MethodScope(classScope,  name, parameters, returnType, staticContext)
      return MethodNode(token, Opcodes.ACC_PUBLIC, ownerClass,  name, FunctionBlockNode(
        token, methodScope, mutableListOf()
      ), methodScope.parameters.map { MethodParameterNode(token, it) }.toMutableList(),  returnType, methodScope, false, annotations)
    }
  }

  override val isAbstract = false
  override val isDefault = false

  override val descriptor get() = AsmUtils.getMethodDescriptor(parameters, returnType)

  override fun toString(): String {
    return "${if (isStatic) "static " else ""}fun $returnType $name(" + parameters.joinToString(separator = ", ", postfix = ") ")
  }

}