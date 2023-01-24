package com.tambapps.marcel.cl

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.tambapps.marcel.compiler.CompilationResult
import com.tambapps.marcel.compiler.JarWriter
import com.tambapps.marcel.compiler.MarcelCompiler
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParsingException
import com.tambapps.marcel.parser.exception.SemanticException
import marcel.lang.Script
import java.io.File
import java.io.IOException
import java.net.URLClassLoader
import kotlin.system.exitProcess

// useful commands to add later: doctor, upgrade
val COMMANDS = listOf("execute", "compile", "doctor", "upgrade")
class Marcl : CliktCommand(help = "MARCel Command Line tool") {
  override fun run() {
    // just run, subcommands will be executed
  }

}
class CompileCommand : CliktCommand(help = "Compiles a Marcel class to a .class file and/or .jar file") {
  private val file by argument().file(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true)
  private val keepClassFile by option("-c", "--keep-class", help = "keep compiled class file after execution").flag()
  private val keepJarFile by option("-j", "--keep-jar", help = "keep compiled jar file after execution").flag()
  private val printStackTrace by option("-p", "--print-stack-trace", help = "print stack trace on compilation error").flag()

  override fun run() {
    val className = generateClassName(file.name)
    val result = compile(file, className, printStackTrace) ?: return
    if (!keepClassFile && !keepJarFile || keepClassFile) { // if no option is specified
      File("$className.class").writeBytes(result.bytes)
    }
    if (keepJarFile) {
      JarWriter().writeScriptJar(result.className, result.bytes, File(file.parentFile, "$className.jar"))
    }
  }
}

class ExecuteCommand(private val scriptArguments: Array<String>) : CliktCommand(help = "Execute a marcel script") {

  private val keepClassFile by option("-c", "--keep-class", help = "keep compiled class file after execution").flag()
  private val keepJarFile by option("-j", "--keep-jar", help = "keep compiled jar file after execution").flag()
  private val printStackTrace by option("-p", "--print-stack-trace", help = "print stack trace on compilation error").flag()
  private val file by argument().file(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true)
  // here for the usage message
  private val arguments by argument(name = "SCRIPT_ARGUMENTS").multiple()

  override fun run() {
    val className = generateClassName(file.name)
    val result = compile(file, className, printStackTrace) ?: return

    if (keepClassFile) {
      File("$className.class").writeBytes(result.bytes)
    }

    val jarFile = File(file.parentFile, "$className.jar")
    JarWriter().writeScriptJar(result.className, result.bytes, jarFile)

    val classLoader = URLClassLoader(arrayOf(jarFile.toURI().toURL()), MarcelCompiler::class.java.classLoader)
    val script = classLoader.loadClass(result.className).getDeclaredConstructor().newInstance() as Script
    try {
      script.run(scriptArguments)
    } finally {
      if (!keepJarFile) {
        jarFile.delete()
      }
    }
  }
}

fun main(args : Array<String>) {
  val arguments = args.toMutableList()
  val containsHelp = arguments.contains("-h") || arguments.contains("--help")
  if (!containsHelp && arguments.firstOrNull() == "execute") {
    arguments.removeAt(0)
  }
  if (arguments.firstOrNull() !in COMMANDS && !containsHelp) {
    // need special behaviour for execute command because there are marcl parameters, and script parameters
    val fileNameIndex = arguments.indexOfFirst { !it.startsWith("-") }
    if (fileNameIndex < 0) {
      println("must provide a file")
      exitProcess(1)
    }
    val executeArguments = arguments.subList(0, fileNameIndex + 1)
    val scriptArguments = arguments.subList(fileNameIndex + 1, arguments.size).toTypedArray()
    ExecuteCommand(scriptArguments).main(executeArguments)
  } else {
    // adding executeCommand for the help message
    Marcl().subcommands(ExecuteCommand(emptyArray()), CompileCommand()).main(arguments)
  }
}

fun compile(file: File, className: String, printStackTrace: Boolean): CompilationResult? {
  return try {
    MarcelCompiler().compile(file.reader(), className)
  } catch (e: IOException) {
    println("An error occurred while reading file: ${e.message}")
    if (printStackTrace) e.printStackTrace()
    return null
  } catch (e: MarcelLexerException) {
    println("Lexer error: ${e.message}")
    if (printStackTrace) e.printStackTrace()
    return null
  } catch (e: MarcelParsingException) {
    println("Parsing error: ${e.message}")
    if (printStackTrace) e.printStackTrace()
    return null
  } catch (e: SemanticException) {
    println("Semantic error: ${e.message}")
    if (printStackTrace) e.printStackTrace()
    return null
  } catch (e: Exception) {
    println("An unexpected error occurred while: ${e.message}")
    if (printStackTrace) e.printStackTrace()
    return null
  }
}

private fun generateClassName(fileName: String): String {
  val i = fileName.indexOf('.')
  return if (i < 0) fileName else fileName.substring(0, i)
}