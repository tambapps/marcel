package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken

abstract class AbstractClassCstNode(
  parentSourceFileNode: SourceFileCstNode,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  override val access: AccessCstNode,
  override val className: String,
  override val superType: TypeCstNode?,
  override val interfaces: List<TypeCstNode>,
  override val isExtensionClass: Boolean,
  override val forExtensionType: TypeCstNode?,
): AbstractCstNode(parentSourceFileNode, tokenStart, tokenEnd), ClassCstNode {

  override val parent: SourceFileCstNode
    get() = super.parent as SourceFileCstNode

  override val annotations: MutableList<AnnotationCstNode> = mutableListOf()
  override val methods: MutableList<MethodCstNode> = mutableListOf()
  override val fields: MutableList<FieldCstNode> = mutableListOf()
  override val constructors: MutableList<ConstructorCstNode> = mutableListOf()
  override val innerClasses: MutableList<ClassCstNode> = mutableListOf()

}