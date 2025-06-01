package com.tambapps.marcel.repl.console

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.expression.FunctionCallNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.operator.VariableAssignmentNode
import com.tambapps.marcel.semantic.processor.extensions.forEachNode
import com.tambapps.marcel.semantic.variable.Variable
import com.tambapps.marcel.semantic.variable.field.MarcelField
import com.tambapps.marcel.semantic.variable.field.MethodField

abstract class AbstractHighlighter<HighlightedString, Builder, Style> constructor(
  private val replCompiler: MarcelReplCompiler,
  private val style: HighlightTheme<Style>,
) {

  private val lexer = MarcelLexer()

  protected abstract fun newBuilder(): Builder
  protected abstract fun build(builder: Builder): HighlightedString

  fun highlight(text: CharSequence): HighlightedString {
    return build(highlightBuilder(text))
  }

  fun highlightBuilder(text: CharSequence): Builder {
    val builder = newBuilder()
    val textStr = text.toString()

    val semanticResult = replCompiler.tryApplySemantic(textStr)

    if (semanticResult != null) {
      val highlightTokenMapBuilder = HighlightTokenMapBuilder()
      for (classNode in semanticResult.classes) {
        highlightTokenMapBuilder.accept(classNode)
      }
      val tokens = semanticResult.tokens
      val tokenMap = highlightTokenMapBuilder.tokenMap
      applyHighlight(text, builder, tokens, tokenMap)
    } else {
      val tokens = tryLex(textStr)
      if (tokens != null) {
        applyHighlight(text, builder, tokens, emptyMap())
      } else {
        highlight(builder, style.default, textStr)
      }
    }
    return builder
  }

  private fun tryLex(text: String): List<LexToken>? =
    try { lexer.lex(text) } catch (e: MarcelLexerException) { null }

  // exclusive end
  protected abstract fun highlight(builder: Builder, style: Style, string: String)

  private fun applyHighlight(text: CharSequence, builder: Builder, tokens: List<LexToken>,
                             tokenMap: Map<LexToken, Style>) {
    for (i in 0 until tokens.size - 1) { // - 1 to avoid handling END_OF_FILE token
      val token = tokens[i]
      val string = text.substring(token.start, token.end)
      val style = when (token.type) {
        TokenType.IDENTIFIER -> tokenMap[token] ?: style.default
        TYPE_INT, TYPE_LONG, TYPE_SHORT, TYPE_FLOAT, TYPE_DOUBLE, TYPE_BOOL, TYPE_BYTE, TYPE_VOID, TYPE_CHAR, FUN, RETURN,
        VALUE_TRUE, VALUE_FALSE, NEW, IMPORT, AS, INLINE, STATIC, FOR, IN, IF, ELSE, NULL, BREAK, CONTINUE, DEF, DO,
        CLASS, EXTENSION, PACKAGE, EXTENDS, IMPLEMENTS, FINAL, SWITCH, WHEN, NOT_WHEN, THIS, SUPER, DUMBBELL, TRY, CATCH, FINALLY, WHILE,
        INSTANCEOF, NOT_INSTANCEOF("!instanceof"), THROW, THROWS, CONSTRUCTOR, DYNOBJ, ASYNC, ENUM, OVERRIDE,
          // visibilities
        VISIBILITY_PUBLIC, VISIBILITY_PROTECTED, VISIBILITY_INTERNAL, VISIBILITY_PRIVATE, -> style.keyword
        TokenType.INTEGER, TokenType.FLOAT -> style.number
        TokenType.BLOCK_COMMENT, TokenType.DOC_COMMENT, TokenType.HASH, TokenType.SHEBANG_COMMENT, TokenType.EOL_COMMENT -> style.comment
        TokenType.OPEN_QUOTE, TokenType.CLOSING_QUOTE, TokenType.REGULAR_STRING_PART,
        TokenType.OPEN_CHAR_QUOTE, TokenType.CLOSING_CHAR_QUOTE,
        TokenType.OPEN_REGEX_QUOTE, TokenType.CLOSING_REGEX_QUOTE,
        TokenType.OPEN_SIMPLE_QUOTE, TokenType.CLOSING_SIMPLE_QUOTE -> style.string
        TokenType.SHORT_TEMPLATE_ENTRY_START, TokenType.LONG_TEMPLATE_ENTRY_START, TokenType.LONG_TEMPLATE_ENTRY_END -> style.stringTemplate
        else -> style.default
      }
      highlight(builder, style, string)
    }
  }

  // maps tokens to styles
  private inner class HighlightTokenMapBuilder {
    val tokenMap = mutableMapOf<LexToken, Style>()

    fun accept(classNode: ClassNode) {
      for (fieldNode in classNode.fields) {
        fieldNode.identifierToken?.let { token -> tokenMap[token] = style.field }
      }
      for (annotation in classNode.annotations) {
        acceptAnnotation(annotation)
      }
      for (annotation in classNode.fields.flatMap { it.annotations }) {
        acceptAnnotation(annotation)
      }
      for (annotation in classNode.methods.flatMap { it.annotations }) {
        acceptAnnotation(annotation)
      }
      for (methodNode in classNode.methods) {
        methodNode.identifierToken?.let { token -> tokenMap[token] = style.function }
        methodNode.blockStatement.forEachNode { node ->
          if (shouldBeProcessed(node)) {
            when (node) {
              is VariableAssignmentNode -> node.identifierToken?.let { token -> tokenMap[token] = variableStyle(node.variable) }
              is ReferenceNode -> tokenMap[node.tokenStart] = variableStyle(node.variable)
              is FunctionCallNode -> tokenMap[node.tokenStart] = style.function
            }
          }
        }
      }
    }

    fun acceptAnnotation(annotationNode: AnnotationNode) {
      tokenMap[annotationNode.tokenStart] = style.annotation
      annotationNode.identifierToken?.let { token -> tokenMap[token] = style.annotation }
    }

    private fun variableStyle(variable: Variable) = when (variable) {
      is MethodField -> style.function
      is MarcelField -> style.field
      else -> style.variable
    }

    private fun shouldBeProcessed(node: AstNode): Boolean {
      return node.tokenEnd != LexToken.DUMMY // this is a mark used to recognize nodes of marcel-generated code (not in the source)
          && !tokenMap.containsKey(node.tokenStart) // don't really remember what this condition is for
    }

  }
}