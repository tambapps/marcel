package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.type.JavaType

class MethodeNode(val name: String,
                  val visibility: Visibility,
                  val returnType: JavaType,
                  val isStatic: Boolean,
                  val isConstructor: Boolean,
                  tokenStart: LexToken, tokenEnd: LexToken) : AbstractAst2Node(tokenStart, tokenEnd) {
                    val instructions = mutableListOf<StatementNode>()

}