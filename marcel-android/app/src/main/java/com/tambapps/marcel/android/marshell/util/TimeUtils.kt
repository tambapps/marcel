package com.tambapps.marcel.android.marshell.util

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import java.time.temporal.UnsupportedTemporalTypeException
import java.util.Locale

object TimeUtils {

  val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
  private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
  private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

  fun humanReadableFormat(duration: Duration, truncateUnit: ChronoUnit = ChronoUnit.MILLIS): String {
    return truncatedTo(duration, truncateUnit).toString()
      .substring(2)
      .replace("(\\d[HMS])(?!$)".toRegex(), "$1")
      .lowercase(Locale.getDefault())
  }

  // copied from jdk, because Duration.truncateTo is only available from Java 9
  private fun truncatedTo(duration: Duration, unit: TemporalUnit): Duration {
    if (unit == ChronoUnit.SECONDS && (duration.seconds >= 0 || duration.nano == 0)) {
      return Duration.ofSeconds(duration.seconds)
    } else if (unit == ChronoUnit.NANOS) {
      return duration
    }
    val unitDur = unit.duration

    val dur = unitDur.toNanos()
    val NANOS_PER_DAY = 86400000000000L
    val SECONDS_PER_DAY = 86400
    val NANOS_PER_SECOND = 1000_000_000L
    if ((NANOS_PER_DAY % dur) != 0L) {
      throw UnsupportedTemporalTypeException("Unit must divide into a standard day without remainder")
    }
    val nod = (duration.seconds % SECONDS_PER_DAY) * NANOS_PER_SECOND + duration.nano
    val result = (nod / dur) * dur
    return duration.plusNanos(result - nod)
  }
  fun smartToString(localDateTime: LocalDateTime): String {
    val date = localDateTime.toLocalDate()
    val dateString = when (date) {
      LocalDate.now() -> "today"
      LocalDate.now().minusDays(1) -> "yesterday"
      LocalDate.now().plusDays(1) -> "tomorrow"
      else -> "the " + DATE_FORMATTER.format(date)
    }

    val timeString = TIME_FORMATTER.format(localDateTime.toLocalTime())

    return "$dateString at $timeString"
  }
}