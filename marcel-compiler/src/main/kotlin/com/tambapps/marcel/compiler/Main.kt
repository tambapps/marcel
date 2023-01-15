package com.tambapps.marcel.compiler

import com.tambapps.marcel.compiler.bytecode.BytecodeWriter
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParser
import com.tambapps.marcel.parser.MarcelParsingException
import java.io.File
import java.io.IOException
import java.net.URLClassLoader

fun main(args : Array<String>) {
  val file = File(args[0])
  if (!file.isFile) {
    println("File $file does not exists or isn't a file")
    return
  }
  val tokens = try {
    MarcelLexer().lex(file.reader())
  } catch (e: IOException) {
    println("An error occurred while reading file: ${e.message}")
    return
  } catch (e: MarcelLexerException) {
    println("Lexer error: ${e.message}")
    e.printStackTrace()
    return
  }

  val className = generateClassName(file.name)
  val ast = try {
    MarcelParser(className, tokens).parse()
  } catch (e: MarcelParsingException) {
    println("Parsing error: ${e.message}")
    e.printStackTrace()
    return
  }

  val classFile = File(file.parentFile, "$className.class")
  val result = try {
    BytecodeWriter().generate(ast)
  } catch (e: Exception) {
    println("Error while writing class: ${e.message}")
    e.printStackTrace()
    return
  }
  classFile.writeBytes(result.bytes)
  // TODO trying load class doesn't work
  val classLoader = URLClassLoader(arrayOf(File(".").toURI().toURL()), BytecodeWriter::class.java.classLoader)
  //classLoader.loadClass(result.className)
}

private fun generateClassName(fileName: String): String {
  val i = fileName.indexOf('.')
  if (i < 0) {
    return fileName
  } else {
    return fileName.substring(0, i)
  }
}