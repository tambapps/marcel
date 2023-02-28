package com.tambapps.marcel.compiler.util

import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.ast.expression.LiteralArrayNode
import com.tambapps.marcel.parser.ast.expression.LiteralMapNode
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.type.JavaType

fun LiteralArrayNode.getElementsType(typeResolver: JavaTypeResolver) =
  if (elements.isNotEmpty()) JavaType.commonType(elements.map { it.accept(typeResolver) })
  else null

fun LiteralMapNode.getKeysType(typeResolver: JavaTypeResolver) =
  JavaType.commonType(entries.map { it.first.accept(typeResolver) })
fun LiteralMapNode.getValuesType(typeResolver: JavaTypeResolver) =
  JavaType.commonType(entries.map { it.second.accept(typeResolver) })

fun ExpressionNode.getType(typeResolver: JavaTypeResolver) = typeResolver.resolve(this)
fun StatementNode.getType(typeResolver: JavaTypeResolver) = typeResolver.resolve(this)