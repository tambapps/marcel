package com.tambapps.marcel.compiler.util

import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.parser.ast.expression.FunctionCallNode


fun FunctionCallNode.getMethod(typeResolver: JavaTypeResolver) =
  if (methodOwnerType != null) typeResolver.findMethodOrThrow(methodOwnerType!!.type, name, arguments)
  else scope.getMethod(name, arguments)