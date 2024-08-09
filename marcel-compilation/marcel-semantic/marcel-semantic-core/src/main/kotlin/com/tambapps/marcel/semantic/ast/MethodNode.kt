package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.statement.BlockStatementNode
import com.tambapps.marcel.semantic.method.AbstractMethod
import com.tambapps.marcel.semantic.method.MarcelMethod
import com.tambapps.marcel.semantic.method.MethodParameter
import com.tambapps.marcel.semantic.type.JavaType

class MethodNode(
  override val name: String,
  override val parameters: MutableList<MethodParameter>,
  override val visibility: Visibility,
  override var returnType: JavaType,
  override val isStatic: Boolean,
  override val asyncReturnType: JavaType?,
  override val tokenStart: LexToken, override val tokenEnd: LexToken,
  override var ownerClass: JavaType,
) : AstNode, AbstractMethod(), Annotable {

  // constructor for non async functions
  constructor(
    name: String,
    parameters: MutableList<MethodParameter>,
    visibility: Visibility,
    returnType: JavaType,
    isStatic: Boolean,
    tokenStart: LexToken, tokenEnd: LexToken,
    ownerClass: JavaType,
  ) : this(name, parameters, visibility, returnType, isStatic, null, tokenStart, tokenEnd, ownerClass)

  companion object {
    fun fromJavaMethod(method: MarcelMethod, tokenStart: LexToken, tokenEnd: LexToken): MethodNode {
      return MethodNode(
        method.name,
        method.parameters.toMutableList(),
        method.visibility,
        method.returnType,
        method.isStatic,
        tokenStart,
        tokenEnd,
        method.ownerClass
      )
    }
  }

  override val annotations: MutableList<AnnotationNode> = mutableListOf()
  override val isConstructor = name == MarcelMethod.CONSTRUCTOR_NAME

  override val isAbstract = false
  override val isDefault = false
  override val isVarArgs = false
  val blockStatement = BlockStatementNode(mutableListOf(), tokenStart, tokenEnd)

}