package com.tambapps.marcel.marshell.console

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.TokenType.*
import com.tambapps.marcel.marshell.console.style.HighlightTheme
import com.tambapps.marcel.parser.ast.MethodNode
import com.tambapps.marcel.parser.ast.expression.FunctionCallNode
import com.tambapps.marcel.parser.ast.expression.ReferenceExpression
import com.tambapps.marcel.parser.ast.expression.VariableAssignmentNode
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.statement.VariableDeclarationNode
import org.jline.reader.Highlighter
import org.jline.reader.LineReader
import org.jline.utils.AttributedString
import org.jline.utils.AttributedStringBuilder
import org.jline.utils.AttributedStyle
import java.util.regex.Pattern

class ReaderHighlighter constructor(private val nodeSupplier: () -> MethodNode?): Highlighter {

  // TODO add coloring on variable/types once parsing will have been implemented
  val lexer = MarcelLexer(false)
  val style = HighlightTheme()

  override fun highlight(reader: LineReader, buffer: String): AttributedString {
    val highlightedString = AttributedStringBuilder()
    val node = nodeSupplier.invoke() ?: return AttributedString(buffer)
    val tokens = lexer.lexSafely(buffer)
    tokens.removeLast() // remove end of file

    for (token in tokens) {
      val string = buffer.substring(token.start, token.end)
      val style = when (token.type) {
        IDENTIFIER -> identifierStyle(token, node)
        TYPE_INT, TYPE_LONG, TYPE_SHORT, TYPE_FLOAT, TYPE_DOUBLE, TYPE_BOOL, TYPE_BYTE, TYPE_VOID, TYPE_CHAR, FUN, RETURN,
        VALUE_TRUE, VALUE_FALSE, NEW, IMPORT, AS, INLINE, STATIC, FOR, IN, IF, ELSE, NULL, BREAK, CONTINUE, DEF,
        CLASS, EXTENSION, PACKAGE, EXTENDS, IMPLEMENTS, FINAL, SWITCH, WHEN, THIS, SUPER,
          // visibilities
        VISIBILITY_PUBLIC, VISIBILITY_PROTECTED, VISIBILITY_INTERNAL, VISIBILITY_PRIVATE -> style.keyword
        INTEGER, FLOAT -> style.number
        BLOCK_COMMENT, DOC_COMMENT, HASH, SHEBANG_COMMENT, EOL_COMMENT -> style.comment
        OPEN_QUOTE, CLOSING_QUOTE, REGULAR_STRING_PART,
        OPEN_CHAR_QUOTE, CLOSING_CHAR_QUOTE,
        OPEN_REGEX_QUOTE, CLOSING_REGEX_QUOTE,
        OPEN_SIMPLE_QUOTE, CLOSING_SIMPLE_QUOTE -> style.string
        SHORT_TEMPLATE_ENTRY_START, LONG_TEMPLATE_ENTRY_START, LONG_TEMPLATE_ENTRY_END -> style.stringTemplate
        else -> AttributedStyle.DEFAULT
      }
      highlight(highlightedString, style, string)
    }
    return highlightedString.toAttributedString()
  }

  private fun identifierStyle(token: LexToken, scriptNode: MethodNode?): AttributedStyle {
    if (scriptNode == null) return AttributedStyle.DEFAULT
    var node = scriptNode.block.find { it.token == token } ?: return AttributedStyle.DEFAULT

    if (node is ExpressionStatementNode) node = node.expression
    return when (node) {
      is VariableAssignmentNode -> {
        val variable = node.scope.findVariable(node.name)
        if (variable != null) style.variable
        else AttributedStyle.DEFAULT
      }
      is ReferenceExpression -> {
        val variable = node.scope.findVariable(node.name)

        if (variable != null) style.variable
        else AttributedStyle.DEFAULT
      }
      is FunctionCallNode -> {
        // TODO
        AttributedStyle.DEFAULT
      }
      else -> AttributedStyle.DEFAULT
    }


/*
    val scope = scopeSupplier.invoke()
    val variable = scope.findVariable(token.value)
    when {
      scope.findVariable(token.value) != null -> style.variable
      scope.findMethodOrThrow(token.value) != null -> style.variable
    }
    var style = style.variable

    println(scope.findVariable("a"))
    AttributedStyle.DEFAULT

 */
  }
  private fun highlight(builder: AttributedStringBuilder, style: AttributedStyle, string: String) {
    builder.style(style)
    builder.append(string)
  }


  override fun setErrorPattern(errorPattern: Pattern) {}

  override fun setErrorIndex(errorIndex: Int) {}

}