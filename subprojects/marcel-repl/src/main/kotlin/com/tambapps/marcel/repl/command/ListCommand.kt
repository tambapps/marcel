package com.tambapps.marcel.repl.command

import com.tambapps.marcel.repl.MarcelShell
import com.tambapps.marcel.repl.printer.Printer
import com.tambapps.marcel.semantic.ast.ImportNode
import com.tambapps.marcel.semantic.type.JavaType
import marcel.lang.Binding

class ListCommand: AbstractShellCommand() {

  override val name = "list"
  override val shortName = "l"
  override val usage = ":list or :list (variables|functions|classes|imports)"
  override val helpDescription = "list defined members"


  override suspend fun run(shell: MarcelShell, args: List<String>, out: Printer) {
    if (args.isEmpty()) {
      out.println("Imports:")
      printImports(shell.imports, out)
      out.println()
      out.println("Classes:")
      printDefinedClasses(shell.definedTypes, out)
      out.println()
      out.println("Functions:")
      printFunctions(shell.definedFunctions, out)
      out.println()
      out.println("Variables:")
      printVariables(shell.binding, out)
      out.println()
    } else {
      when (val arg = args.first().lowercase()) {
        "v", "var", "variable", "variables" -> printVariables(shell.binding, out)
        "f", "func", "function", "functions" -> printFunctions(shell.definedFunctions, out)
        "c", "class", "classes" -> printDefinedClasses(shell.definedTypes, out)
        "i", "import", "imports" -> printImports(shell.imports, out)
        else -> out.println("Unknown value $arg. Provide 'variables', 'functions' or 'classes'")
      }
    }
  }

  private suspend fun printVariables(binding: Binding, out: Printer) {
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

  private suspend fun printFunctions(definedMethods: Collection<com.tambapps.marcel.parser.cst.MethodNode>, out: Printer) {
    if (definedMethods.isEmpty()) {
      out.println("No functions defined")
      return
    }
    definedMethods.forEach {
      out.println(it)
    }
  }

  private suspend fun printDefinedClasses(classes: List<JavaType>, out: Printer) {
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

  private suspend fun printImports(imports: Collection<ImportNode>, out: Printer) {
    if (imports.isEmpty()) {
      out.println("No imports added")
      return
    }
    imports.forEach { out.println(it) }
  }

}