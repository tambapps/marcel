package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.cst.statement.StatementCstNode
import com.tambapps.marcel.parser.cst.visitor.ClassCstNodeVisitor

class ScriptCstNode(
  parentSourceFileNode: SourceFileCstNode,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  className: String
) : AbstractClassCstNode(parentSourceFileNode, tokenStart, tokenEnd,
  AccessCstNode(parentSourceFileNode, tokenStart, tokenEnd, isStatic = false, isInline = false, isFinal = false, TokenType.VISIBILITY_PUBLIC, isExplicit = false),
  className, null, emptyList(), false, null
) {

  override val isEnum = false
  override val isScript = true

  val runMethodStatements: MutableList<StatementCstNode> = mutableListOf()

  val isNotEmpty get() = runMethodStatements.isNotEmpty()
      || methods.isNotEmpty()
      || fields.isNotEmpty()
      || constructors.isNotEmpty()

  override fun <T> accept(visitor: ClassCstNodeVisitor<T>) = visitor.visit(this)

}