package com.tambapps.marcel.compiler.util

import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.parser.ast.AstNode
import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.ast.expression.FunctionCallNode
import com.tambapps.marcel.parser.ast.expression.LiteralArrayNode
import com.tambapps.marcel.parser.ast.expression.LiteralMapNode
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.type.JavaType


fun FunctionCallNode.getMethod(typeResolver: JavaTypeResolver) =
  if (methodOwnerType != null) typeResolver.findMethodOrThrow(methodOwnerType!!.getType(typeResolver), name,
    arguments.map { it.accept(typeResolver) })
  else scope.getMethod(name, arguments.map { it.accept(typeResolver) })

fun LiteralArrayNode.getElementsType(typeResolver: JavaTypeResolver) =
  JavaType.commonType(elements.map { it.accept(typeResolver) })

fun LiteralMapNode.getKeysType(typeResolver: JavaTypeResolver) =
  JavaType.commonType(entries.map { it.first.accept(typeResolver) })
fun LiteralMapNode.getValuesType(typeResolver: JavaTypeResolver) =
  JavaType.commonType(entries.map { it.second.accept(typeResolver) })

fun ExpressionNode.getType(typeResolver: JavaTypeResolver) = typeResolver.resolve(this)
fun StatementNode.getType(typeResolver: JavaTypeResolver) = typeResolver.resolve(this)