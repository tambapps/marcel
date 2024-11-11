package com.tambapps.marcel.cl

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.net.URI
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
  @ValueSource(ints = [1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 13, 15])
  fun aocDay_2023(day: Int) = aocDay(2023, day)

  // AOC that takes long to run
  @ParameterizedTest(name = "AOC 2023 day {arguments}")
  @ValueSource(ints = [12, 14, 16]) // 5 is not put there because it is very long
  fun aocDay_2023_longDays(day: Int) = aocDay(2023, day)

  fun aocDay(year: Int, day: Int) {
    File("input.txt").writeText(URI(URL_TEMPLATE.format(year, day, "input.txt")).toURL().readText())
    val text = URI(URL_TEMPLATE.format(year, day, "solution.mcl")).toURL().readText()
    evalSource("AOC$day", text)
  }
}