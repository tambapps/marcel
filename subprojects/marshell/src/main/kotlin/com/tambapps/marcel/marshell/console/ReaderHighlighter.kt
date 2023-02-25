package com.tambapps.marcel.marshell.console

import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.TokenType.*
import com.tambapps.marcel.marshell.console.style.HighlightTheme
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.parser.ast.MethodNode
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.ast.expression.FunctionCallNode
import com.tambapps.marcel.parser.ast.expression.IndexedReferenceExpression
import com.tambapps.marcel.parser.ast.expression.IndexedVariableAssignmentNode
import com.tambapps.marcel.parser.ast.expression.ReferenceExpression
import com.tambapps.marcel.parser.ast.expression.VariableAssignmentNode
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import org.jline.reader.Highlighter
import org.jline.reader.LineReader
import org.jline.utils.AttributedString
import org.jline.utils.AttributedStringBuilder
import org.jline.utils.AttributedStyle
import java.util.regex.Pattern

class ReaderHighlighter constructor(
  private val typeResolver: JavaTypeResolver,
  private val replCompiler: MarcelReplCompiler
): Highlighter {

  private val lexer = MarcelLexer(false)
  private val style = HighlightTheme()

  override fun highlight(reader: LineReader, text: String): AttributedString {
    val highlightedString = AttributedStringBuilder()
    val parseResult = replCompiler.tryParse(text)
    val tokens = parseResult?.tokens?.toMutableList() ?: lexer.lexSafely(text)
    tokens.removeLast() // remove end of file
    val scriptNode = parseResult?.scriptNode ?: replCompiler.parserResult?.scriptNode
    val node = scriptNode?.methods?.find { it.name == "run" && it.parameters.size == 1 }

    for (token in tokens) {
      val string = text.substring(token.start, token.end)
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
    val node = scriptNode.block.find {
      it.token == token && (it is VariableAssignmentNode || it is ReferenceExpression || it is IndexedVariableAssignmentNode
          || it is IndexedReferenceExpression || it is FunctionCallNode)

    } ?: return AttributedStyle.DEFAULT

    return when (node) {
      is VariableAssignmentNode -> variableHighlight(node, node.name)
      is ReferenceExpression -> variableHighlight(node, node.name)
      is FunctionCallNode -> {
        val method = try { node.getMethod(typeResolver) } catch (e: MarcelSemanticException) { null }
        if (method != null) style.function
        else AttributedStyle.DEFAULT
      }
      else -> AttributedStyle.DEFAULT
    }
  }

  private fun variableHighlight(node: ScopedNode<*>, name: String): AttributedStyle {
    val variable = node.scope.findVariable(name)
    return if (variable != null) style.variable
    else AttributedStyle.DEFAULT
  }
  private fun highlight(builder: AttributedStringBuilder, style: AttributedStyle, string: String) {
    builder.style(style)
    builder.append(string)
  }


  override fun setErrorPattern(errorPattern: Pattern) {}

  override fun setErrorIndex(errorIndex: Int) {}

}