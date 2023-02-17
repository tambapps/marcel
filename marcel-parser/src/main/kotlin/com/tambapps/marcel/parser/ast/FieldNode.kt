package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.scope.ClassField
import com.tambapps.marcel.parser.type.JavaType

class FieldNode(type: JavaType, name: String, owner: JavaType, access: Int) : AstNode,
  ClassField(type, name, owner, access) {
}