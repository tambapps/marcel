package com.tambapps.marcel.repl.command

import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.ast.ImportNode
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.repl.MarcelShell
import com.tambapps.marcel.repl.printer.Printer
import marcel.lang.Binding

class ListCommand: AbstractShellCommand() {

  override val name = "list"
  override val shortName = "l"
  override val usage = ":list or :list (variables|functions|classes|imports)"
  override val helpDescription = "list defined members"


  override fun run(shell: MarcelShell, args: List<String>, out: Printer) {
    if (args.isEmpty()) {
      out.println("Imports:")
      printImports(shell.imports, out)
      out.println()
      out.println("Classes:")
      printDefinedClasses(shell.definedClasses, out)
      out.println()
      out.println("Functions:")
      printFunctions(shell.lastNode, out)
      out.println()
      out.println("Variables:")
      printVariables(shell.binding, out)
      out.println()
    } else {
      when (val arg = args.first().lowercase()) {
        "v", "variable", "variables" -> printVariables(shell.binding, out)
        "f", "function", "functions" -> printVariables(shell.binding, out)
        "c", "class", "classes" -> printDefinedClasses(shell.definedClasses, out)
        "i", "import", "imports" -> printImports(shell.imports, out)
        else -> out.println("Unknown value $arg. Provide 'variables', 'functions' or 'classes'")
      }
    }
  }

  private fun printVariables(binding: Binding, out: Printer) {
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

  private fun printFunctions(classNode: ClassNode?, out: Printer) {
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

  private fun printDefinedClasses(classes: List<JavaType>, out: Printer) {
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

  private fun printImports(imports: List<ImportNode>, out: Printer) {
    if (imports.isEmpty()) {
      out.println("No imports added")
      return
    }
    imports.forEach { out.println(it) }
  }

}