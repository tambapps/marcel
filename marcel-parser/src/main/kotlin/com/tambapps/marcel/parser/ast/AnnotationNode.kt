package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.type.JavaType

// TODO verify Target when writing annotation (e.g. that this annotation can be used on a class/method/etc)
class AnnotationNode(override val token: LexToken, val javaType: JavaType) : AstNode {
}