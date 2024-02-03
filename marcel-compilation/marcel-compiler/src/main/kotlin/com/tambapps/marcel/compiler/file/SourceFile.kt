package com.tambapps.marcel.compiler.file

import java.io.File

interface SourceFile {
  val fileName: String
  val text: String
  val className: String

  companion object {
    fun generateClassName(fileName: String): String {
      val i = fileName.indexOf('.')
      return if (i < 0) fileName else fileName.substring(0, i)
    }
    fun fromFile(file: File): SourceFile {
      return FileSourceFile(file)
    }

    fun from(fileName: String, text: String): SourceFile {
      return BasicSourceFile(fileName, text)
    }
  }
}
