package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.statement.BlockStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.method.AbstractMethod
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType

class MethodNode(override val name: String,
                 override val visibility: Visibility,
                 override val returnType: JavaType,
                 override val isStatic: Boolean,
                 override val tokenStart: LexToken, override val tokenEnd: LexToken,
                 override val ownerClass: JavaType,
) : Ast2Node, AbstractMethod(), Annotable {

  override val annotations: MutableList<AnnotationNode> = mutableListOf()
  override val parameters: MutableList<MethodParameterNode> = mutableListOf()
  lateinit var blockStatement: BlockStatementNode
  override val isConstructor = name == JavaMethod.CONSTRUCTOR_NAME

  override val isAbstract = false
  override val isDefault = false
  override val actualReturnType = returnType
}