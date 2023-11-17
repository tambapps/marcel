package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.SemanticHelper
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTyped

open class ClassNode(
  override val type: JavaType,
  val visibility: Visibility,
  tokenStart: LexToken, tokenEnd: LexToken
) : AbstractAst2Node(tokenStart, tokenEnd), JavaTyped {
  val superType: JavaType get() = type.superType!!

  val fields = mutableListOf<FieldNode>()
  val methods = mutableListOf<MethodNode>()
  val annotations = mutableListOf<AnnotationNode>()
  val innerClasses = mutableListOf<ClassNode>()

  val constructors get() = methods.filter { it.isConstructor }
  val constructorCount get() = methods.count { it.isConstructor }

  fun getOrCreateStaticInitialisationMethod(): MethodNode {
    val m = methods.find { it.name == JavaMethod.STATIC_INITIALIZATION_BLOCK }
    if (m != null) return m
    val newMethod = SemanticHelper.staticInitialisationMethod(this)
    methods.add(newMethod)
    return newMethod
  }
}