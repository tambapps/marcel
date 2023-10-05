package com.tambapps.marcel.semantic

import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.MethodeNode
import com.tambapps.marcel.semantic.type.JavaType

class MarcelSemantic {

  fun apply(cst: SourceFileCstNode): ClassNode {
    // TODO parse package if any
    val className = cst.fileName
    if (cst.instructions.isNotEmpty()) {
      val classNode = ClassNode(className, cst.tokenStart, cst.tokenEnd)
      val runMethod = MethodeNode("run", Visibility.PUBLIC, JavaType.Object, isStatic = false, isConstructor = false,
        tokenStart = cst.instructions.first().tokenStart, tokenEnd = cst.instructions.last().tokenEnd)

      return classNode
    } else {
      TODO()
    }
  }

}