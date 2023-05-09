package com.tambapps.marcel.android.marshell.util

import java.time.Duration
import java.util.Locale

object TimeUtils {

  fun humanReadableFormat(duration: Duration): String? {
    return duration.toString()
      .substring(2)
      .replace("(\\d[HMS])(?!$)".toRegex(), "$1")
      .lowercase(Locale.getDefault())
  }
}