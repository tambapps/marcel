package com.tambapps.marcel.compiler.file

import java.io.File

internal class FileSourceFile(private val file: File): SourceFile {
  override val fileName: String = file.name
  override val className = SourceFile.generateClassName(fileName)
  override val text: String get() = file.readText()

}