package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.visitor.ClassCstNodeVisitor

open class RegularClassCstNode constructor(
  parentSourceFileNode: SourceFileCstNode,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  access: AccessCstNode,
  className: String,
  superType: TypeCstNode?,
  interfaces: List<TypeCstNode>,
  forExtensionType: TypeCstNode?
) : AbstractClassCstNode(
  parentSourceFileNode,
  tokenStart,
  tokenEnd,
  access,
  className,
  superType,
  interfaces,
  forExtensionType
) {

  override fun <T> accept(visitor: ClassCstNodeVisitor<T>) = visitor.visit(this)

  override val isScript = false
  override val isEnum = false

}