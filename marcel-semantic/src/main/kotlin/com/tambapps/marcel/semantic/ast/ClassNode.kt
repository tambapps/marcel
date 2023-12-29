package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.SemanticHelper
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTyped

open class ClassNode constructor(
  override val type: JavaType,
  val visibility: Visibility,
  val forExtensionType: JavaType?, // if the class is an extension class
  val isStatic: Boolean,
  val isScript: Boolean,
  val fileName: String,
  tokenStart: LexToken, tokenEnd: LexToken
) : AbstractAstNode(tokenStart, tokenEnd), JavaTyped {
  val superType: JavaType get() = type.superType!!
  val isExtensionClass get() = forExtensionType != null

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

  override fun toString() = StringBuilder().apply {
    if (isExtensionClass) append("extension ")
    append("class ${type.simpleName}")
    if (isExtensionClass) append("for " + forExtensionType?.simpleName)
    if (superType != JavaType.Object) {
      append(" extends ")
      append(superType.simpleName)
    }
    if (type.directlyImplementedInterfaces.isNotEmpty()) {
      append(" implements ")
      type.directlyImplementedInterfaces.joinTo(buffer = this, separator = ",", transform = { it.simpleName })
    }
  }.toString()
}