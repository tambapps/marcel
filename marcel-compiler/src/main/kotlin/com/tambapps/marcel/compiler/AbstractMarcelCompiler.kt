package com.tambapps.marcel.compiler

import com.tambapps.marcel.compiler.annotation.DelegateAnnotationProcessor
import com.tambapps.marcel.parser.ast.ModuleNode

abstract class AbstractMarcelCompiler(protected val compilerConfiguration: CompilerConfiguration) {

  constructor(): this(CompilerConfiguration())

  private val moduleNodeVisitors: List<ModuleNodeVisitor> = listOf(
    DelegateAnnotationProcessor()
  )

  protected fun visitAst(ast: ModuleNode, typeResolver: JavaTypeResolver) {
    moduleNodeVisitors.forEach { it.visit(ast, typeResolver) }
  }
}