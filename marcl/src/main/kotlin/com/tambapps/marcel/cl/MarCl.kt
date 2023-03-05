package com.tambapps.marcel.cl

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.compiler.JarWriter
import com.tambapps.marcel.compiler.MarcelCompiler
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParserException
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import marcel.lang.MarcelClassLoader
import marcel.lang.URLMarcelClassLoader
import java.io.File
import java.io.IOException
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
  private val keepClassFiles by option("-c", "--class", help = "Compile to class").flag()
  private val keepJarFile by option("-j", "--jar", help = "Compile to jar").flag()
  private val printStackTrace by option("-p", "--print-stack-trace", help = "print stack trace on compilation error").flag()

  override fun run() {
    val className = generateClassName(file.name)
    compile(file, className, keepClassFiles, keepJarFile, printStackTrace)
  }
}

class ExecuteCommand(private val scriptArguments: Array<String>) : CliktCommand(help = "Execute a marcel script") {

  private val keepClassFiles by option("-c", "--keep-class", help = "keep compiled class files after execution").flag()
  private val keepJarFile by option("-j", "--keep-jar", help = "keep compiled jar file after execution").flag()
  private val printStackTrace by option("-p", "--print-stack-trace", help = "print stack trace on compilation error").flag()
  private val file by argument().file(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true)
  // here for the usage message
  private val arguments by argument(name = "SCRIPT_ARGUMENTS").multiple()

  override fun run() {
    val className = generateClassName(file.name)
    val scriptLoader = URLMarcelClassLoader()

    // we want to keep jar because we will run it
    // TODO doesn't work when script has package
    val jarFile = compile(file, className, keepClassFiles, true, printStackTrace, scriptLoader) ?: return

    try {
      // and then run it with the new classLoader
      scriptLoader.loadScript(className, jarFile).run(scriptArguments)
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
  val marcl = Marcl().subcommands(ExecuteCommand(emptyArray()), CompileCommand())
  if (arguments.firstOrNull() !in COMMANDS && !containsHelp) {
    // need special behaviour for execute command because there are marcl parameters, and script parameters
    val fileNameIndex = arguments.indexOfFirst { !it.startsWith("-") }
    if (fileNameIndex < 0) {
      println("must provide a file")
      // show help
      marcl.main(arrayOf("-h"))
      exitProcess(1)
    }
    val executeArguments = arguments.subList(0, fileNameIndex + 1)
    val scriptArguments = arguments.subList(fileNameIndex + 1, arguments.size).toTypedArray()
    ExecuteCommand(scriptArguments).main(executeArguments)
  } else {
    // adding executeCommand for the help message
    marcl.main(arguments)
  }
}

fun compile(file: File, className: String, keepClassFiles: Boolean, keepJarFile: Boolean, printStackTrace: Boolean, scriptLoader: MarcelClassLoader? = null): File? {
  val classes = try {
    MarcelCompiler(CompilerConfiguration(dumbbellEnabled = true)).compile(scriptLoader, file.readText(), className)
  } catch (e: IOException) {
    println("An error occurred while reading file: ${e.message}")
    if (printStackTrace) e.printStackTrace()
    return null
  } catch (e: MarcelLexerException) {
    println("Lexer error: ${e.message}")
    if (printStackTrace) e.printStackTrace()
    return null
  } catch (e: MarcelParserException) {
    println("Parsing error: ${e.message}")
    if (printStackTrace) e.printStackTrace()
    return null
  } catch (e: MarcelSemanticException) {
    println("Semantic error: ${e.message}")
    if (printStackTrace) e.printStackTrace()
    return null
  } catch (e: Exception) {
    println("An unexpected error occurred while: ${e.message}")
    if (printStackTrace) e.printStackTrace()
    return null
  }

  for (compiledClass in classes) {
    if (!keepClassFiles && !keepJarFile || keepClassFiles) { // if no option is specified
      File("${compiledClass.className}.class").writeBytes(compiledClass.bytes)
    }
  }

  if (!keepJarFile) return null
  val jarFile = File(file.parentFile, "$className.jar")
  JarWriter(jarFile).use {
    it.writeClass(classes)
  }
  return jarFile
}

private fun generateClassName(fileName: String): String {
  val i = fileName.indexOf('.')
  return if (i < 0) fileName else fileName.substring(0, i)
}