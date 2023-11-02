package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.statement.BlockStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.method.AbstractMethod
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.method.MethodParameter
import com.tambapps.marcel.semantic.type.JavaType

class MethodNode(override val name: String,
                 override val parameters: List<MethodParameter>,
                 override val visibility: Visibility,
                 override val returnType: JavaType,
                 override val isStatic: Boolean,
                 override val tokenStart: LexToken, override val tokenEnd: LexToken,
                 override val ownerClass: JavaType,
) : Ast2Node, AbstractMethod(), Annotable {

  companion object {
    fun fromJavaMethod(method: JavaMethod, tokenStart: LexToken, tokenEnd: LexToken): MethodNode {
      return MethodNode(method.name, method.parameters, method.visibility, method.returnType, method.isStatic, tokenStart, tokenEnd, method.ownerClass)
    }
  }
  override val annotations: MutableList<AnnotationNode> = mutableListOf()
  lateinit var blockStatement: BlockStatementNode
  override val isConstructor = name == JavaMethod.CONSTRUCTOR_NAME

  override val isAbstract = false
  override val isDefault = false
  override val actualReturnType = returnType
}