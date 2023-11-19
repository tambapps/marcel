package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AbstractAstNode
import com.tambapps.marcel.parser.ast.AstInstructionNode

interface StatementNode: AstInstructionNode {

  // returns whether this statement is a block statment with nothing in it or not
  fun statesNothing() = false
}
abstract class AbstractStatementNode(token: LexToken) : AbstractAstNode(token), StatementNode