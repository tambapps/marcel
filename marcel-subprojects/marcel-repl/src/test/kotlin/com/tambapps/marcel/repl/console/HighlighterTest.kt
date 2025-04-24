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
      Arguments.of("int a = 1"),

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

val THEME = HighlightTheme(
  variable = Unit,
  function = Unit,
  string = Unit,
  stringTemplate = Unit,
  keyword = Unit,
  comment = Unit,
  number = Unit,
  default = Unit,
)

private class HighlighterImpl(
  replCompiler: MarcelReplCompiler
) : AbstractHighlighter<String, StringBuilder, Unit>(replCompiler, THEME) {

  override fun newBuilder() = StringBuilder()

  override fun build(builder: StringBuilder) = builder.toString()

  override fun highlight(builder: StringBuilder, style: Unit, string: String) {
    builder.append(string)
  }

}