package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AbstractAstNode
import com.tambapps.marcel.parser.ast.AstInstructionNode

interface StatementNode: AstInstructionNode
abstract class AbstractStatementNode(token: LexToken) : AbstractAstNode(token), StatementNode