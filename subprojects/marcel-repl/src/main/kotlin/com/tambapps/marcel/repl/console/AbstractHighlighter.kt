package com.tambapps.marcel.repl.console

import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.TokenType.*
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.parser.ast.MethodNode
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.ast.expression.FunctionCallNode
import com.tambapps.marcel.parser.ast.expression.IndexedReferenceExpression
import com.tambapps.marcel.parser.ast.expression.IndexedVariableAssignmentNode
import com.tambapps.marcel.parser.ast.expression.ReferenceExpression
import com.tambapps.marcel.parser.ast.expression.VariableAssignmentNode
import com.tambapps.marcel.parser.exception.MarcelSemanticLegacyException

abstract class AbstractHighlighter<T, Style> constructor(
  private val typeResolver: JavaTypeResolver,
  private val replCompiler: MarcelReplCompiler
) {

  private val lexer = MarcelLexer(false)
  
  abstract val variableStyle: Style
  abstract val functionStyle: Style
  abstract val stringStyle: Style
  abstract val stringTemplateStyle: Style
  abstract val keywordStyle: Style
  abstract val commentStyle: Style
  abstract val numberStyle: Style
  abstract val defaultStyle: Style

  abstract fun newHighlightedString(text: CharSequence): T

  fun highlight(text: CharSequence): T {
    val highlightedString = newHighlightedString(text)
    val textStr = text.toString()
    val parseResult = replCompiler.tryParse(textStr)
    val tokens = parseResult?.tokens?.toMutableList() ?: lexer.lexSafely(textStr)
    tokens.removeLastOrNull() // remove end of file
    val scriptNode = parseResult?.scriptNode ?: replCompiler.parserResult?.scriptNode
    val node = scriptNode?.methods?.find { it.name == "run" && it.parameters.size == 1 }

    for (token in tokens) {
      val string = text.substring(token.start, token.end)
      val style = when (token.type) {
        IDENTIFIER -> identifierStyle(token, node)
        TYPE_INT, TYPE_LONG, TYPE_SHORT, TYPE_FLOAT, TYPE_DOUBLE, TYPE_BOOL, TYPE_BYTE, TYPE_VOID, TYPE_CHAR, FUN, RETURN,
        VALUE_TRUE, VALUE_FALSE, NEW, IMPORT, AS, INLINE, STATIC, FOR, IN, IF, ELSE, NULL, BREAK, CONTINUE, DEF,
        CLASS, EXTENSION, PACKAGE, EXTENDS, IMPLEMENTS, FINAL, SWITCH, WHEN, THIS, SUPER, DUMBBELL, TRY, CATCH, FINALLY,
        INSTANCEOF, THROW, THROWS, CONSTRUCTOR, DYNOBJ,
          // visibilities
        VISIBILITY_PUBLIC, VISIBILITY_PROTECTED, VISIBILITY_INTERNAL, VISIBILITY_PRIVATE -> keywordStyle
        INTEGER, FLOAT -> numberStyle
        BLOCK_COMMENT, DOC_COMMENT, HASH, SHEBANG_COMMENT, EOL_COMMENT -> commentStyle
        OPEN_QUOTE, CLOSING_QUOTE, REGULAR_STRING_PART,
        OPEN_CHAR_QUOTE, CLOSING_CHAR_QUOTE,
        OPEN_REGEX_QUOTE, CLOSING_REGEX_QUOTE,
        OPEN_SIMPLE_QUOTE, CLOSING_SIMPLE_QUOTE -> stringStyle
        SHORT_TEMPLATE_ENTRY_START, LONG_TEMPLATE_ENTRY_START, LONG_TEMPLATE_ENTRY_END -> stringTemplateStyle
        else -> defaultStyle
      }
      highlight(highlightedString, style, string, token.start, token.end)
    }
    return highlightedString
  }

  protected abstract fun highlight(highlightedString: T, style: Style, string: String, startIndex: Int,
                                   // exclusive
                                   endIndex: Int)


  private fun identifierStyle(token: LexToken, scriptNode: MethodNode?): Style {
    if (scriptNode == null) return defaultStyle
    val node = scriptNode.block.find {
      it.token == token && (it is VariableAssignmentNode || it is ReferenceExpression || it is IndexedVariableAssignmentNode
          || it is IndexedReferenceExpression || it is FunctionCallNode)

    } ?: return defaultStyle

    return when (node) {
      is VariableAssignmentNode -> variableHighlight(node, node.name)
      is ReferenceExpression -> variableHighlight(node, node.name)
      is FunctionCallNode -> {
        val method = try { node.getMethod(typeResolver) } catch (e: MarcelSemanticLegacyException) { null }
        if (method != null) functionStyle
        else defaultStyle
      }
      else -> defaultStyle
    }
  }

  private fun variableHighlight(node: ScopedNode<*>, name: String): Style {
    val variable = node.scope.findVariable(name)
    return if (variable != null) variableStyle
    else defaultStyle
  }

}