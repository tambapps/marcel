package com.tambapps.marcel.repl.console

import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.compiler.util.getType
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.parser.ast.AstInstructionNode
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.ast.expression.GetFieldAccessOperator
import com.tambapps.marcel.parser.ast.expression.ReferenceExpression
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import com.tambapps.marcel.parser.scope.MarcelField
import com.tambapps.marcel.parser.type.JavaMethod

abstract class AbstractShellCompleter<T>(
  private val compiler: MarcelReplCompiler,
  private val typeResolver: JavaTypeResolver
) {

  abstract fun newCandidate(complete: String): T

  fun complete(lastLine: String, candidates: MutableList<T>) {
    var line = lastLine
    if (line.contains(" ")) line = line.substring(line.lastIndexOf(' ') + 1)
    val endsWithDot = line.endsWith(".")
    val result = compiler.tryParseWithoutUpdate(
      if (endsWithDot) line.substring(0, line.lastIndex) else line
    ) ?: return

    val scriptNode = result.scriptNode ?: return
    var lastNode: AstInstructionNode = scriptNode.methods.find { it.name == "run" && it.parameters.size == 1 }!!
      .block
      .statements
      .lastOrNull() ?: return
    if (lastNode is ExpressionStatementNode) lastNode = lastNode.expression
    if (endsWithDot && lastNode is ReferenceExpression) {
      lastNode = GetFieldAccessOperator(lastNode.token, lastNode, ReferenceExpression(lastNode.token, lastNode.scope, ""), false)
    }
    if (lastNode is GetFieldAccessOperator) {
      completeClassMember(lastNode, endsWithDot, candidates)
    } else if (lastNode is ReferenceExpression) {
      completeScriptMember(lastNode, scriptNode, candidates)
    }
  }

  private fun completeScriptMember(lastNode: ReferenceExpression, scriptNode: ClassNode, candidates: MutableList<T>) {
    val prefix = lastNode.name
    typeResolver.getDeclaredMethods(scriptNode.type).forEach {
      if (it.name.startsWith(prefix) && it.name != "run" && it.name != "main") {
        val suffix = if (it.parameters.isEmpty()) "()" else "("
        candidates.add(newCandidate(it.name + suffix))
      }
    }
    typeResolver.getDeclaredFields(scriptNode.type).forEach {
      if (it.name.startsWith(prefix)) {
        candidates.add(newCandidate(it.name))
      }
    }
  }

  private fun completeClassMember(lastNode: GetFieldAccessOperator, endsWithDot: Boolean, candidates: MutableList<T>) {
    val type = try { lastNode.leftOperand.getType(typeResolver) } catch (e: MarcelSemanticException) { null } ?: return

    val methodFilter: (JavaMethod) -> Boolean
    val fieldFilter: (MarcelField) -> Boolean
    if (endsWithDot) {
      methodFilter = { true }
      fieldFilter = { true }
    } else {
      val prefix = lastNode.rightOperand.name
      methodFilter = { it.name.startsWith(prefix) }
      fieldFilter = { it.name.startsWith(prefix) }
    }
    typeResolver.getMethods(type).forEach {
      if (methodFilter.invoke(it)) {
        val suffix = if (it.parameters.isEmpty()) "()" else "("
        candidates.add(newCandidate(it.name + suffix))
      }
    }
    typeResolver.getFields(type).forEach {
      if (fieldFilter.invoke(it)) {
        candidates.add(newCandidate(it.name))
      }
    }
  }
}