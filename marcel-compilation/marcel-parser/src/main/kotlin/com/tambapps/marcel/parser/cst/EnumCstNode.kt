package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.visitor.ClassCstNodeVisitor

open class EnumCstNode constructor(
  parentSourceFileNode: SourceFileCstNode,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  access: AccessCstNode,
  className: String, // full class name. also handles inner class names
  val names: List<String>
) : AbstractClassCstNode(parentSourceFileNode, tokenStart, tokenEnd, access, className, null, emptyList(), false, null) {
  override val isEnum = true
  override val isScript = false

  override fun <T> accept(visitor: ClassCstNodeVisitor<T>) = visitor.visit(this)

}