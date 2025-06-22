package com.tambapps.marcel.marshell.command

import com.tambapps.marcel.marshell.Marshell
import com.tambapps.marcel.parser.cst.MethodCstNode
import com.tambapps.marcel.semantic.processor.imprt.MutableImportResolver
import com.tambapps.marcel.semantic.symbol.type.JavaType
import marcel.lang.Binding
import java.io.PrintStream

class ListCommand: AbstractShellCommand() {

  override val name = "list"
  override val shortName = "l"
  override val usage = ":list or :list (variables|functions|classes|imports)"
  override val helpDescription = "list defined members"


  override fun run(shell: Marshell, args: List<String>, out: PrintStream) {
    if (args.isEmpty()) {
      out.println("Imports:")
      printImports(shell.evaluator.imports, out)
      out.println()
      out.println("Classes:")
      printDefinedClasses(shell.evaluator.definedTypes, out)
      out.println()
      out.println("Functions:")
      printFunctions(shell.evaluator.definedFunctions, out)
      out.println()
      out.println("Variables:")
      printVariables(shell.evaluator.binding, out)
      out.println()
    } else {
      when (val arg = args.first().lowercase()) {
        "v", "var", "variable", "variables" -> printVariables(shell.evaluator.binding, out)
        "f", "func", "function", "functions" -> printFunctions(shell.evaluator.definedFunctions, out)
        "c", "class", "classes" -> printDefinedClasses(shell.evaluator.definedTypes, out)
        "i", "import", "imports" -> printImports(shell.evaluator.imports, out)
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

  private fun printFunctions(definedMethods: Collection<MethodCstNode>, out: PrintStream) {
    if (definedMethods.isEmpty()) {
      out.println("No functions defined")
      return
    }
    definedMethods.sortedWith(compareBy({ it.name }, { it.parameters.size })).forEach {
      out.println(it)
    }
  }

  private fun printDefinedClasses(classes: Collection<JavaType>, out: PrintStream) {
    if (classes.isEmpty()) {
      out.println("No classes defined")
      return
    }
    for (c in classes.sortedBy { it.simpleName }) {
      val className = c.type.className
      val displayedName = className.substring(className.indexOf('$') + 1)
      out.print("class $displayedName")
      if (c.superType != null && c.superType != JavaType.Object) out.print(" extends ${c.superType!!.simpleName}")
      out.println()
    }
  }

  private fun printImports(imports: MutableImportResolver, out: PrintStream) {
    if (imports.isEmpty()) {
      out.println("No imports added")
      return
    }
    for ((importKey, javaType) in imports.typeImports) {
      if (javaType.simpleName == importKey) {
        out.println("import $javaType")
      } else {
        out.println("import $javaType as $importKey")
      }
    }
    for (wildcardPrefix in imports.wildcardTypeImportPrefixes) {
      out.println("import $wildcardPrefix.*")
    }
    for ((memberName, javaType) in imports.staticMemberImports) {
      out.println("import $javaType.$memberName")
    }
  }

}