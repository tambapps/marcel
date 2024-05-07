package com.tambapps.marcel.android.marshell.util

import java.io.IOException
import java.io.InputStream


fun readText(inputStream: InputStream?): Result<String> {
  if (inputStream == null) {
    return Result.failure(IOException("Couldn't open file"))
  }
  return try {
    Result.success(inputStream.reader().use { it.readText() })
  } catch (e: IOException) {
    Result.failure(e)
  }
}
