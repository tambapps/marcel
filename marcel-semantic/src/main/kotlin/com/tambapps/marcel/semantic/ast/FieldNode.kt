package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.variable.field.JavaClassField

class FieldNode constructor(override val token: LexToken, type: JavaType, name: String, owner: JavaType,
                            val initialValue: ExpressionNode?,
                            override val annotations: List<AnnotationNode>,
                            override val isFinal: Boolean,
                            override val visibility: Visibility,
                            override val isStatic: Boolean,
                            override val tokenStart: LexToken,
                            override val tokenEnd: LexToken,
) : Ast2Node, Annotable,
  JavaClassField(type, name, owner) {

  override fun toString(): String {
    var s =  "${type.simpleName} $name"
    if (initialValue != null) s += " $initialValue"
    return s
  }
}