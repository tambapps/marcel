package com.tambapps.marcel.compiler

import java.io.File
import java.util.function.Supplier

data class SourceFile(val fileName: String,

                      private val textSupplier: Supplier<String>) {
  val text get() = textSupplier.get()
  val className = generateClassName(fileName)


  private fun generateClassName(fileName: String): String {
    val i = fileName.indexOf('.')
    return if (i < 0) fileName else fileName.substring(0, i)
  }
  companion object {
    fun fromFile(file: File): SourceFile {
      return SourceFile(file.name, file::readText)
    }
  }
}