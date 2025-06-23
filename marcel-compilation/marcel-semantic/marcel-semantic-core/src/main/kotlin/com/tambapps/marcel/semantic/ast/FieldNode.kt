package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.symbol.Visibility
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness
import com.tambapps.marcel.semantic.symbol.variable.field.JavaClassField

class FieldNode constructor(
  type: JavaType, name: String, owner: JavaType,
  override val nullness: Nullness,
  override val annotations: List<AnnotationNode>,
  override val isFinal: Boolean,
  override val visibility: Visibility,
  override val isStatic: Boolean,
  override val tokenStart: LexToken,
  override val tokenEnd: LexToken,
  val isSynthetic: Boolean = false,
  val isEnum: Boolean = false,
  val identifierToken: LexToken? = null
) : AstNode, Annotable,
  JavaClassField(type, name, owner) {

  override fun toString(): String {
    return "${type.simpleName} $name"
  }
}