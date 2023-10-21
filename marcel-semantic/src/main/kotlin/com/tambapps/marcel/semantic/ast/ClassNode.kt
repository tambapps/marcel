package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTyped

class ClassNode(
  override val type: JavaType,
  val visibility: Visibility,
  tokenStart: LexToken, tokenEnd: LexToken
) : AbstractAst2Node(tokenStart, tokenEnd), JavaTyped {
  val superType = type.superType!!

  val fields = mutableListOf<FieldNode>()
  val methods = mutableListOf<MethodNode>()
  val annotations = mutableListOf<AnnotationNode>()

  val constructorCount get() = methods.count { it.isConstructor }

}