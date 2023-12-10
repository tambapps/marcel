package com.tambapps.marcel.cl

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.net.URL

@Disabled("Only to be ran manually. These are just to check I didn't break compatibility with anything")
class AdventOfCodeTest: AbstractCompilerTest() {

  companion object {
    const val URL_TEMPLATE = "https://raw.githubusercontent.com/tambapps/advent-of-code/main/%d/%02d/%s"

    @JvmStatic
    @AfterAll
    fun clean() {
      File("input.txt").delete()
    }
  }

  @ParameterizedTest(name = "AOC 2022 day {arguments}")
  @ValueSource(ints = [1, 2, 3, 4, 6, 7])
  fun aocDay_2022(day: Int) = aocDay(2022, day)

  @ParameterizedTest(name = "AOC 2023 day {arguments}")
  @ValueSource(ints = [1, 2, 3, 4, 6, 7, 8, 9, 10])
  fun aocDay_2023(day: Int) = aocDay(2023, day)

  fun aocDay(year: Int, day: Int) {
    File("input.txt").writeText(URL(URL_TEMPLATE.format(year, day, "input.txt")).readText())
    val text = URL(URL_TEMPLATE.format(year, day, "solution.mcl")).readText()
    evalSource("AOC$day", text)
  }
}