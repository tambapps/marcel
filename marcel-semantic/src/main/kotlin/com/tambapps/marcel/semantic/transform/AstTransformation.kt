package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.Ast2Node
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import com.tambapps.marcel.semantic.type.NotLoadedJavaType

interface AstTransformation {
  fun init(typeResolver: JavaTypeResolver)
  fun transformType(javaType: NotLoadedJavaType, annotation: AnnotationNode)
  fun transform(node: Ast2Node, annotation: AnnotationNode)
}
