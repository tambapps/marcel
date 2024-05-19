package com.tambapps.marcel.repl.console

import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.ReplMarcelSymbolResolver
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver
import marcel.lang.URLMarcelClassLoader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class HighlighterTest {

  private val classLoader = URLMarcelClassLoader()
  private val symbolResolver = ReplMarcelSymbolResolver(classLoader)
  private val replCompiler = MarcelReplCompiler(CompilerConfiguration(), classLoader, symbolResolver)
  private val highlighter = HighlighterImpl(symbolResolver, replCompiler)

  companion object {
    @JvmStatic
    fun test() = listOf(
      Arguments.of("a = 1"),
      Arguments.of("&"),
    )
  }

  @ParameterizedTest
  @MethodSource
  fun test(input: String) {
    val actualOutput = highlighter.highlight(input)
    assertEquals(input, actualOutput)
  }

}


private class HighlighterImpl(symbolResolver: MarcelSymbolResolver,
                              replCompiler: MarcelReplCompiler
) : AbstractHighlighter<CharSequence, Unit>(symbolResolver, replCompiler) {
  override val variableStyle = Unit
  override val functionStyle = Unit
  override val stringStyle = Unit
  override val stringTemplateStyle = Unit
  override val keywordStyle = Unit
  override val commentStyle = Unit
  override val numberStyle = Unit
  override val defaultStyle = Unit

  override fun newHighlightedString(text: CharSequence) = text


  override fun highlight(highlightedString: CharSequence, style: Unit, string: String, startIndex: Int, endIndex: Int) {

  }


}