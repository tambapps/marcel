package marcel.lang.extensions

import marcel.lang.Markdown
import marcel.lang.compile.ExtensionClass

@ExtensionClass
object MdStringExtensions {

  @JvmStatic
  fun getMd(`$self`: CharSequence): Markdown {
    return Markdown.of(`$self`.toString())
  }

  @JvmStatic
  fun toMd(`$self`: StringBuilder): Markdown {
    return Markdown.of(`$self`.toString())
  }

  @JvmStatic
  fun toMd(`$self`: StringBuffer): Markdown {
    return Markdown.of(`$self`.toString())
  }
}
