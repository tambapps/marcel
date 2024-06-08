package com.tambapps.marcel.repl.console

import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.ReplMarcelSymbolResolver
import marcel.lang.URLMarcelClassLoader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class HighlighterTest {

  private lateinit var classLoader: URLMarcelClassLoader
  private lateinit var replCompiler: MarcelReplCompiler
  private lateinit var highlighter: HighlighterImpl

  companion object {
    @JvmStatic
    fun test() = listOf(
      Arguments.of("a = 1"),
      Arguments.of("&"),
      Arguments.of("""
        |a = 1
        | // some comment
        |b = 2
      """.trimMargin()),
    )
  }

  @BeforeEach
  fun init() {
    classLoader = URLMarcelClassLoader()
    replCompiler = MarcelReplCompiler(CompilerConfiguration(), classLoader, ReplMarcelSymbolResolver(classLoader))
    highlighter = HighlighterImpl(replCompiler)
  }

  @ParameterizedTest
  @MethodSource
  fun test(input: String) {
    val actualOutput = highlighter.highlight(input)
    assertEquals(input, actualOutput)
  }

}


private class HighlighterImpl(
  replCompiler: MarcelReplCompiler
) : AbstractHighlighter<String, StringBuilder, Unit>(replCompiler) {
  override val variableStyle = Unit
  override val functionStyle = Unit
  override val stringStyle = Unit
  override val stringTemplateStyle = Unit
  override val keywordStyle = Unit
  override val commentStyle = Unit
  override val numberStyle = Unit
  override val defaultStyle = Unit


  override fun newBuilder() = StringBuilder()

  override fun build(builder: StringBuilder) = builder.toString()

  override fun highlight(builder: StringBuilder, style: Unit, string: String) {
    builder.append(string)
  }


}