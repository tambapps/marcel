package com.tambapps.marcel.repl.command

import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.repl.MarcelShell
import marcel.lang.Binding
import java.io.PrintStream

class ListCommand: AbstractShellCommand() {

  override val name = "list"
  override val shortName = "l"
  override val usage = ":list or :list (variables|functions|classes)"
  override val helpDescription = "list defined members"


  override fun run(shell: MarcelShell, args: List<String>, out: PrintStream) {
    if (args.isEmpty()) {
      out.println("Variables:")
      printVariables(shell.binding, out)
      out.println()
      out.println("Functions:")
      printFunctions(shell.lastNode, out)
      out.println()
      out.println("Classes:")
      printDefinedClasses(shell.definedClasses, out)
      out.println()
    } else {
      val arg = args.first().lowercase()
      when (arg) {
        "v", "variable", "variables" -> printVariables(shell.binding, out)
        "f", "function", "functions" -> printVariables(shell.binding, out)
        "c", "class", "classes" -> printDefinedClasses(shell.definedClasses, out)
        else -> out.println("Unknown value $arg. Provide 'variables', 'functions' or 'classes'")
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

  private fun printDefinedClasses(classes: List<JavaType>, out: PrintStream) {
    if (classes.isEmpty()) {
      out.println("No classes defined")
      return
    }
    for (c in classes) {
      val className = c.type.className
      val displayedName = className.substring(className.indexOf('$') + 1)
      out.print("class $displayedName")
      if (c.superType != null && c.superType != JavaType.Object) out.print(" extends ${c.superType!!.simpleName}")
      out.println()
    }
  }

}