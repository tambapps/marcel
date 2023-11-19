package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.expression.JavaConstantExpression
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.type.LoadedJavaAnnotation

class AnnotationNode constructor(override val token: LexToken, type: JavaType,
  val specifiedAttributes: List<Pair<String, JavaConstantExpression>>) : AstNode, LoadedJavaAnnotation(type) {

  }