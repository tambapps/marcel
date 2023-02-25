package com.tambapps.marcel.marshell.command

import com.tambapps.marcel.marshell.Shell
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.type.JavaType
import marcel.lang.Binding
import java.io.PrintStream

class ListCommand: AbstractShellCommand() {

  override val name = "list"
  override val shortName = "l"
  override val usage = ":list or :list (Variables, Functions, Classes)"
  override val helpDescription = "list defined members"


  override fun run(shell: Shell, args: List<String>, out: PrintStream) {
    if (args.isEmpty()) {
      out.println("Variables:")
      printVariables(shell.binding, out)
      out.println()
      out.println("Functions:")
      printFunctions(shell.lastNode, out)
      out.println()
      out.println("Classes:")
      printInnerClasses(shell.lastNode, out)
      out.println()
    } else {
      when (args.first().lowercase()) {
        "v", "variable", "variables" -> printVariables(shell.binding, out)
        "f", "function", "functions" -> printVariables(shell.binding, out)
        "c", "class", "classes" -> printInnerClasses(shell.lastNode, out)
      }
    }
  }

  private fun printVariables(binding: Binding, out: PrintStream) {
    if (binding.variables.isEmpty()) {
      out.println("No variables defined")
    }
    binding.variables.forEach { (key, value) ->
      if (value == null) {
        out.println("$key = $value")
      } else {
        val type = value.javaClass.simpleName
        out.println("$type $key = $value")
      }
    }
  }

  private fun printFunctions(classNode: ClassNode?, out: PrintStream) {
    if (classNode == null) {
      out.println("No functions defined")
      return
    }
    val definedMethods = classNode.methods.filter {
      !it.isGetter && !it.isSetter && it.name != "main" && it.name != "run" && !it.isConstructor
    }
    if (definedMethods.isEmpty()) {
      out.println("No functions defined")
      return
    }
    definedMethods.forEach {
      out.println(it)
    }
  }

  private fun printInnerClasses(classNode: ClassNode?, out: PrintStream) {
    if (classNode == null || classNode.innerClasses.isEmpty()) {
      out.println("No classes defined")
      return
    }
    for (c in classNode.innerClasses) {
      val className = c.type.className
      val displayedName = className.substring(className.indexOf('$') + 1)
      out.print("class $displayedName")
      if (c.superType != JavaType.Object) out.print(" extends ${c.superType.simpleName}")
      out.println()
      for (innerC in c.innerClasses) {
        printInnerClasses(innerC, out)
      }
    }
  }

}