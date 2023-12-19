package com.tambapps.marcel.semantic.transform;

import com.tambapps.marcel.semantic.ast.AnnotationNode;
import com.tambapps.marcel.semantic.ast.Ast2Node;
import com.tambapps.marcel.semantic.type.JavaTypeResolver;
import com.tambapps.marcel.semantic.type.NotLoadedJavaType;

public interface AstTransformation {

  void init(JavaTypeResolver typeResolver);
  void transformType(NotLoadedJavaType javaType);
  void transform(Ast2Node node, AnnotationNode annotation);

}
