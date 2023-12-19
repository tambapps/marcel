package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import com.tambapps.marcel.semantic.type.NotLoadedJavaType

interface AstTransformation {
  fun init(typeResolver: JavaTypeResolver)
  fun transformType(javaType: NotLoadedJavaType, annotation: AnnotationNode)
  fun transform(node: AstNode, annotation: AnnotationNode)
}
