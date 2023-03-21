package com.tambapps.marcel.repl.command

import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.ast.ImportNode
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.repl.MarcelShell
import com.tambapps.marcel.repl.printer.SuspendPrinter
import marcel.lang.Binding

class ListCommand: AbstractShellCommand() {

  override val name = "list"
  override val shortName = "l"
  override val usage = ":list or :list (variables|functions|classes|imports)"
  override val helpDescription = "list defined members"


  override suspend fun run(shell: MarcelShell, args: List<String>, out: SuspendPrinter) {
    if (args.isEmpty()) {
      out.suspendPrintln("Imports:")
      printImports(shell.imports, out)
      out.suspendPrintln()
      out.suspendPrintln("Classes:")
      printDefinedClasses(shell.definedClasses, out)
      out.suspendPrintln()
      out.suspendPrintln("Functions:")
      printFunctions(shell.lastNode, out)
      out.suspendPrintln()
      out.suspendPrintln("Variables:")
      printVariables(shell.binding, out)
      out.suspendPrintln()
    } else {
      when (val arg = args.first().lowercase()) {
        "v", "variable", "variables" -> printVariables(shell.binding, out)
        "f", "function", "functions" -> printVariables(shell.binding, out)
        "c", "class", "classes" -> printDefinedClasses(shell.definedClasses, out)
        "i", "import", "imports" -> printImports(shell.imports, out)
        else -> out.suspendPrintln("Unknown value $arg. Provide 'variables', 'functions' or 'classes'")
      }
    }
  }

  private suspend fun printVariables(binding: Binding, out: SuspendPrinter) {
    if (binding.variables.isEmpty()) {
      out.suspendPrintln("No variables defined")
    }
    binding.variables.forEach { (key, value) ->
      if (value == null) {
        out.suspendPrintln("$key = $value")
      } else {
        val type = value.javaClass.simpleName
        out.suspendPrintln("$type $key = $value")
      }
    }
  }

  private suspend fun printFunctions(classNode: ClassNode?, out: SuspendPrinter) {
    if (classNode == null) {
      out.suspendPrintln("No functions defined")
      return
    }
    val definedMethods = classNode.methods.filter {
      !it.isGetter && !it.isSetter && it.name != "main" && it.name != "run" && !it.isConstructor
    }
    if (definedMethods.isEmpty()) {
      out.suspendPrintln("No functions defined")
      return
    }
    definedMethods.forEach {
      out.suspendPrintln(it)
    }
  }

  private suspend fun printDefinedClasses(classes: List<JavaType>, out: SuspendPrinter) {
    if (classes.isEmpty()) {
      out.suspendPrintln("No classes defined")
      return
    }
    for (c in classes) {
      val className = c.type.className
      val displayedName = className.substring(className.indexOf('$') + 1)
      out.suspendPrint("class $displayedName")
      if (c.superType != null && c.superType != JavaType.Object) out.suspendPrint(" extends ${c.superType!!.simpleName}")
      out.suspendPrintln()
    }
  }

  private suspend fun printImports(imports: Collection<ImportNode>, out: SuspendPrinter) {
    if (imports.isEmpty()) {
      out.suspendPrintln("No imports added")
      return
    }
    imports.forEach { out.suspendPrintln(it) }
  }

}