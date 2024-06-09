package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTyped

open class ClassNode(
  override val type: JavaType,
  val visibility: Visibility,
  val forExtensionType: JavaType?, // if the class is an extension class
  val isStatic: Boolean,
  val isScript: Boolean,
  val isEnum: Boolean,
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