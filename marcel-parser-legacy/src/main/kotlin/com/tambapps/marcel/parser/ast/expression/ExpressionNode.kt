package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AbstractAstNode
import com.tambapps.marcel.parser.ast.AstInstructionNode

interface ExpressionNode: AstInstructionNode {

}

abstract class AbstractExpressionNode(token: LexToken) : AbstractAstNode(token), ExpressionNode