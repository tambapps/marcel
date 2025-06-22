package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.symbol.Visibility
import com.tambapps.marcel.semantic.ast.statement.BlockStatementNode
import com.tambapps.marcel.semantic.symbol.method.AbstractMethod
import com.tambapps.marcel.semantic.symbol.method.MarcelMethod
import com.tambapps.marcel.semantic.symbol.method.MethodParameter
import com.tambapps.marcel.semantic.symbol.type.JavaType

class MethodNode constructor(
  override val name: String,
  override val parameters: MutableList<MethodParameter>,
  override val visibility: Visibility,
  override var returnType: JavaType,
  override val isStatic: Boolean,
  override val asyncReturnType: JavaType?,
  override val tokenStart: LexToken, override val tokenEnd: LexToken,
  override var ownerClass: JavaType,
  override val isVarArgs: Boolean = false,
  override val isSynthetic: Boolean = false,
  val identifierToken: LexToken? = null,
) : AstNode, AbstractMethod(), Annotable {

  override val isFinal: Boolean
    get() = false

  // constructor for non-async functions
  constructor(
    name: String,
    parameters: MutableList<MethodParameter>,
    visibility: Visibility,
    returnType: JavaType,
    isStatic: Boolean,
    tokenStart: LexToken, tokenEnd: LexToken,
    ownerClass: JavaType,
    isVarArgs: Boolean = false,
    isSynthetic: Boolean = false,
  ) : this(name, parameters, visibility, returnType, isStatic, null, tokenStart, tokenEnd, ownerClass, isVarArgs, isSynthetic)

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
        method.ownerClass,
        method.isVarArgs,
        method.isSynthetic
      )
    }
  }

  override val annotations: MutableList<AnnotationNode> = mutableListOf()
  override val isConstructor = name == MarcelMethod.CONSTRUCTOR_NAME

  override val isAbstract = false
  override val isDefault = false
  val blockStatement = BlockStatementNode(mutableListOf(), tokenStart, tokenEnd)

}