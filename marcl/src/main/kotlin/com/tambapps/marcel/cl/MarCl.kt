package com.tambapps.marcel.cl

import com.tambapps.marcel.compiler.JarWriter
import com.tambapps.marcel.compiler.MarcelCompiler
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParsingException
import com.tambapps.marcel.parser.exception.SemanticException
import java.io.File
import java.io.IOException
import java.net.URLClassLoader


fun main(args : Array<String>) {
  // TODO accept command line parameters (e.g. -c for compile to class)
  val file = File(args[0])
  if (!file.isFile) {
    println("File $file does not exists or isn't a file")
    return
  }

  val className = generateClassName(file.name)

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
  } catch (e: SemanticException) {
    println("Semantic error: ${e.message}")
    e.printStackTrace()
    return
  } catch (e: Exception) {
    println("An unexpected error occurred while: ${e.message}")
    e.printStackTrace()
    return
  }

  // TODO for debugging purpose
  File("$className.class").writeBytes(result.bytes)

  // TODO delete me once run
  val jarFile = File(file.parentFile, "$className.jar")
  JarWriter().writeScriptJar(result.className, result.bytes, jarFile)

  val classLoader = URLClassLoader(arrayOf(jarFile.toURI().toURL()), MarcelCompiler::class.java.classLoader)
  val clazz = classLoader.loadClass(result.className)
  clazz.getMethod("run", Array<String>::class.java).invoke(clazz.getDeclaredConstructor().newInstance(), args)
}

private fun generateClassName(fileName: String): String {
  val i = fileName.indexOf('.')
  if (i < 0) {
    return fileName
  } else {
    return fileName.substring(0, i)
  }
}