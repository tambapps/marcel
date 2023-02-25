package com.tambapps.marcel.marshell.console

import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.compiler.util.getType
import com.tambapps.marcel.marshell.repl.MarcelReplCompiler
import com.tambapps.marcel.parser.ast.AstInstructionNode
import com.tambapps.marcel.parser.ast.expression.GetFieldAccessOperator
import com.tambapps.marcel.parser.ast.expression.ReferenceExpression
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import com.tambapps.marcel.parser.scope.MarcelField
import com.tambapps.marcel.parser.type.JavaMethod
import org.jline.reader.Candidate
import org.jline.reader.Completer
import org.jline.reader.LineReader
import org.jline.reader.ParsedLine

class MarshellCompleter(
  private val compiler: MarcelReplCompiler,
  private val typeResolver: JavaTypeResolver
  ): Completer {

  override fun complete(reader: LineReader, parsedLine: ParsedLine, candidates: MutableList<Candidate>) {
    var line = parsedLine.line()
    if (parsedLine.cursor() < line.lastIndex) return
    if (line.contains(" ")) line = line.substring(line.lastIndexOf(' ') + 1)
    val endsWithDot = line.endsWith(".")
    val result = compiler.tryParseWithoutUpdate(
      if (endsWithDot) line.substring(0, line.lastIndex) else line
    ) ?: return

    var lastNode: AstInstructionNode = result.scriptNode.methods.find { it.name == "run" && it.parameters.size == 1 }!!
      .block
      .statements
      .lastOrNull() ?: return
    if (lastNode is ExpressionStatementNode) lastNode = lastNode.expression
    if (endsWithDot && lastNode is ReferenceExpression) {
      lastNode = GetFieldAccessOperator(lastNode.token, lastNode, ReferenceExpression(lastNode.token, lastNode.scope, ""), false)
    }
    if (lastNode !is GetFieldAccessOperator) return
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
    typeResolver.getDeclaredMethods(type).filter(methodFilter).forEach {
      val suffix = if (it.parameters.isEmpty()) "()" else "("
      candidates.add(Candidate(it.name + suffix))
    }
    typeResolver.getDeclaredFields(type).filter(fieldFilter).forEach {
      candidates.add(Candidate(it.name))
    }
  }
}