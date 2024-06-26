package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken

open class ClassCstNode constructor(
  parentSourceFileNode: SourceFileCstNode,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val access: AccessCstNode,
  val className: String, // full class name. also handles inner class names
  val superType: TypeCstNode?,
  val interfaces: List<TypeCstNode>,
  val forExtensionType: TypeCstNode?,
) : AbstractCstNode(parentSourceFileNode, tokenStart, tokenEnd) {

  override val parent: SourceFileCstNode
    get() = super.parent as SourceFileCstNode

  val isExtensionClass: Boolean get() = forExtensionType != null

  val annotations: MutableList<AnnotationCstNode> = mutableListOf()
  val methods: MutableList<MethodCstNode> = mutableListOf()
  val fields: MutableList<FieldCstNode> = mutableListOf()
  val constructors: MutableList<ConstructorCstNode> = mutableListOf()
  val innerClasses: MutableList<ClassCstNode> = mutableListOf()

}