package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.expression.JavaConstantExpression
import com.tambapps.marcel.parser.type.JavaType

// TODO verify Target when writing annotation (e.g. that this annotation can be used on a class/method/etc)
// TODO will need a annotation type
class AnnotationNode(override val token: LexToken, val javaType: JavaType,
  val attributes: List<Pair<String, JavaConstantExpression>>) : AstNode {
}