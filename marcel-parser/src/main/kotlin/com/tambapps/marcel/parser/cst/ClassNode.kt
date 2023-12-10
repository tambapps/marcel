package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken

open class ClassNode(
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val access: AccessNode,
  val className: String, // full class name. also handles inner class names
  val superType: TypeNode?,
  val interfaces: List<TypeNode>,
  val forExtensionType: TypeNode?,
) : AbstractCstNode(null, tokenStart, tokenEnd) {

  val isExtensionClass: Boolean get() = forExtensionType != null

  val annotations: MutableList<AnnotationNode> = mutableListOf()
  val methods: MutableList<MethodNode> = mutableListOf()
  val fields: MutableList<FieldNode> = mutableListOf()
  val constructors: MutableList<ConstructorNode> = mutableListOf()
  val innerClasses: MutableList<ClassNode> = mutableListOf()

}