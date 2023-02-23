package com.tambapps.marcel.marshell.console.style

import org.jline.utils.AttributedStyle

class HighlightStyle {
  val keyword = AttributedStyle.BOLD.foreground(AttributedStyle.RED)
  val function = AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW)
  val type = AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA)
  val string = AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN)
  val stringTemplate = AttributedStyle.BOLD.foreground(
  AttributedStyle.YELLOW
  )
  val number = AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN)
  val comment = AttributedStyle.DEFAULT.foreground(200, 200, 200)
  val parenthesis = AttributedStyle.DEFAULT.foreground(
  AttributedStyle.BRIGHT
  )
  val typeParameter = AttributedStyle.DEFAULT.foreground(
  AttributedStyle.BLUE
  )
  val identifier = AttributedStyle.DEFAULT.foreground(
  AttributedStyle.YELLOW
  )}