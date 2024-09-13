package marcel.lang

import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.node.Node
import org.commonmark.parser.Parser

/**
 * Class used to render a string as markdown in Marshell
 */
class Markdown private constructor(val source: String, val node: Node) {
  companion object {
    @JvmStatic
    val PARSER: Parser = Parser.builder().extensions(listOf(TablesExtension.create())).build()

    @JvmStatic
    fun of(source: String): Markdown {
      val node = PARSER.parse(source)
      return Markdown(source, node)
    }

    @JvmStatic
    fun lr(): String = System.lineSeparator() + System.lineSeparator()

    @JvmStatic
    fun h1(value: Any?) = h(1, value)

    @JvmStatic
    fun h2(value: Any?) = h(2, value)

    @JvmStatic
    fun h3(value: Any?) = h(3, value)

    @JvmStatic
    fun h4(value: Any?) = h(4, value)

    @JvmStatic
    fun h5(value: Any?) = h(5, value)

    @JvmStatic
    fun h6(value: Any?) = h(6, value)

    @JvmStatic
    fun link(value: Any?, url: String) = "[$value]($url) "

    @JvmStatic
    fun h(level: Int, value: Any?): String {
      val builder = StringBuilder()
      for (i in 0 until level) {
        builder.append('#')
      }
      return builder.append(" ").append(value).append(System.lineSeparator()).toString()
    }
  }

  override fun toString(): String {
    return source
  }
}
