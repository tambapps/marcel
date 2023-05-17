package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.type.JavaType

// TODO verify Target when writting annotation (e.g. that this annotation can be used on a class/method/etc)
// TODO handle annotation parameters
// TODO write them when compiling
class AnnotationNode(override val token: LexToken, val javaType: JavaType) : AstNode {
}