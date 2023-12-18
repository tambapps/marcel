package com.tambapps.marcel.semantic.transform;

import com.tambapps.marcel.parser.cst.CstNode;
import com.tambapps.marcel.semantic.ast.AnnotationNode;

public interface CstTransformation {

  void transform(CstNode node, AnnotationNode annotation);
}
