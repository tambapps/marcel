package com.tambapps.marcel.marshell.console.style

import org.jline.utils.AttributedStyle

class HighlightTheme {
  val keyword = AttributedStyle.BOLD.foreground(AttributedStyle.RED)
  val function = AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW)
  val variable = AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA)

  val type = AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA)
  val string = AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN)
  val stringTemplate = AttributedStyle.BOLD.foreground(
  AttributedStyle.YELLOW
  )
  val number = AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN)
  val comment = AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT)
  val typeParameter = AttributedStyle.DEFAULT.foreground(
  AttributedStyle.BLUE
  )
}