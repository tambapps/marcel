package com.tambapps.marcel.compiler

import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParsingException
import java.io.File
import java.io.IOException

fun main(args : Array<String>) {
  val file = File(args[0])
  if (!file.isFile) {
    println("File $file does not exists or isn't a file")
    return
  }

  val className = generateClassName(file.name)
  val classFile = File(file.parentFile, "$className.class")

  val result = try {
    MarcelCompiler().compile(file.reader(), className)
  } catch (e: IOException) {
    println("An error occurred while reading file: ${e.message}")
    return
  } catch (e: MarcelLexerException) {
    println("Lexer error: ${e.message}")
    e.printStackTrace()
    return
  } catch (e: MarcelParsingException) {
    println("Parsing error: ${e.message}")
    e.printStackTrace()
    return
  } catch (e: Exception) {
    println("An unexpected error occured while: ${e.message}")
    e.printStackTrace()
    return
  }

  classFile.writeBytes(result.bytes)
  // TODO trying load class doesn't work (maybe it is just on mac?)
  //val classLoader = URLClassLoader(arrayOf(File(".").toURI().toURL()), BytecodeWriter::class.java.classLoader)
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