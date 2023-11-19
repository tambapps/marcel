package com.tambapps.marcel.compiler

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

private class BasicSourceFile(override val fileName: String, override val text: String, override val className: String) : SourceFile {
  constructor(fileName: String, text: String): this(fileName, text, SourceFile.generateClassName(fileName))
}

private class FileSourceFile(private val file: File): SourceFile {
  override val fileName: String = file.name
  override val className = SourceFile.generateClassName(fileName)
  override val text: String get() = file.readText()

}