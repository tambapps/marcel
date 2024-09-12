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
    fun h1(text: String?) = h(1, text)

    @JvmStatic
    fun h2(text: String?) = h(2, text)

    @JvmStatic
    fun h3(text: String?) = h(3, text)

    @JvmStatic
    fun h4(text: String?) = h(4, text)

    @JvmStatic
    fun h5(text: String?) = h(5, text)

    @JvmStatic
    fun h6(text: String?) = h(6, text)

    @JvmStatic
    fun link(text: String, url: String) = "[$text]($url)"

    @JvmStatic
    fun h(level: Int, text: String?): String {
      val builder = StringBuilder()
      for (i in 0 until level) {
        builder.append('#')
      }
      return builder.append(" ").append(text).toString()
    }
  }

  override fun toString(): String {
    return source
  }
}
