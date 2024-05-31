package com.tambapps.marcel.semantic.exception

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.type.JavaType

class MemberNotVisibleException constructor(
  token: LexToken,
  member: Any,
  fromType: JavaType
): MarcelSemanticException(token, "$member is not visible from $fromType") {

  constructor(node: CstNode, member: Any, fromType: JavaType): this(node.token, member, fromType)

}