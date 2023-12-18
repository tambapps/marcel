package com.tambapps.marcel.compiler.transform

import com.tambapps.marcel.parser.cst.ClassNode as ClassCstNode
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.SourceFileNode
import com.tambapps.marcel.semantic.MarcelSemantic
import com.tambapps.marcel.semantic.transform.CstTransformation
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import java.lang.annotation.ElementType

class CstTransformer(
  private val typeResolver: JavaTypeResolver
) {

  fun transform(semantic: MarcelSemantic) {


  }

  private fun collectTransformations(semantic: MarcelSemantic): List<Pair<CstNode, CstTransformation>> {
   /* for (s in semantic.classes) {

    }

    */
    TODO()
  }

  private fun doCollectTransformations(semantic: MarcelSemantic, list: MutableList<Pair<CstNode, CstTransformation>>, classNode: ClassCstNode): List<Pair<CstNode, CstTransformation>> {
   // classNode.annotations.map { semantic.visit(it, ElementType.TYPE) }
     // .filter { it.type.an }

    TODO()
  }
}