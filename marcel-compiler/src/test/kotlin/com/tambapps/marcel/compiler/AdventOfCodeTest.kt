package com.tambapps.marcel.compiler

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.net.URL

@Disabled("Only to be ran manually. These are just to check I didn't break compatibility with anything")
class AdventOfCodeTest: AbstractCompilerTest() {

  companion object {
    const val URL_TEMPLATE = "https://raw.githubusercontent.com/tambapps/advent-of-code/main/%02d/%s"

    @JvmStatic
    @AfterAll
    fun clean() {
      File("input.txt").delete()
    }
  }

  @ParameterizedTest(name = "AOC day {arguments}")
  @ValueSource(ints = [1, 2, 3, 4, 6, 7])
  fun aocDay(day: Int) {
    val inputText = URL(URL_TEMPLATE.format(day, "input.txt")).readText()
    File("input.txt").writeText(inputText)
    val text = URL(URL_TEMPLATE.format(day, "solution.mcl")).readText()
    evalSource("AOC$day", text)
  }
}