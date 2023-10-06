package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.method.AbstractMethod
import com.tambapps.marcel.semantic.type.JavaType

class MethodNode(override val name: String,
                 override val visibility: Visibility,
                 override val returnType: JavaType,
                 override val isStatic: Boolean,
                 override val isConstructor: Boolean,
                 override val tokenStart: LexToken, override val tokenEnd: LexToken,
                 override val annotations: List<AnnotationNode>,
                 override val ownerClass: JavaType,
                 override val parameters: List<MethodParameterNode>
) : Ast2Node, AbstractMethod(), Annotable {
                    val instructions = mutableListOf<StatementNode>()

  override val isAbstract = false
  override val isDefault = false
  override val actualReturnType = returnType
}