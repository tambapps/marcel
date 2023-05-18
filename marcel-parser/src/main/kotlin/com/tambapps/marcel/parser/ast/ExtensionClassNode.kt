package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaType

class ExtensionClassNode(
  token: LexToken,
  scope: Scope,
  access: Int,
  type: JavaType,
  superType: JavaType,
  isScript: Boolean,
  methods: MutableList<MethodNode>,
  fields: MutableList<FieldNode>,
  innerClasses: MutableList<ClassNode>,
  annotations: List<AnnotationNode>,
  val extendingClass: JavaType
) : ClassNode(token, scope, access, type, superType, isScript, methods, fields, innerClasses, annotations) {
}