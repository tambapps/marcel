package com.tambapps.marcel.compiler.file

internal class BasicSourceFile(override val fileName: String, override val text: String, override val className: String) : SourceFile {
  constructor(fileName: String, text: String): this(fileName, text, SourceFile.generateClassName(fileName))
}
