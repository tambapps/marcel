package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.parser.cst.visitor.ClassCstNodeVisitor

interface ClassCstNode: CstNode {
  val isScript: Boolean
  val isEnum: Boolean
  // does not support declaring interfaces for now
  val isInterface: Boolean get() = false
  fun <T> accept(visitor: ClassCstNodeVisitor<T>): T

  val access: AccessCstNode

  /**
   * Full class name. also handles inner class names
   */
  val className: String
  val superType: TypeCstNode?
  val interfaces: List<TypeCstNode>
  val forExtensionType: TypeCstNode?

  override val parent: SourceFileCstNode

  val isExtensionClass: Boolean get() = forExtensionType != null

  val annotations: MutableList<AnnotationCstNode>
  val methods: MutableList<MethodCstNode>
  val fields: MutableList<FieldCstNode>
  val constructors: MutableList<ConstructorCstNode>
  val innerClasses: MutableList<ClassCstNode>
}
