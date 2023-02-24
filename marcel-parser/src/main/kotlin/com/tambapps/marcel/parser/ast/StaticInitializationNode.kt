package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.expression.FunctionBlockNode
import com.tambapps.marcel.parser.scope.MethodScope
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import org.objectweb.asm.Opcodes

class StaticInitializationNode private constructor(
  token: LexToken,
  classNode: ClassNode,
  scope: MethodScope,
) : MethodNode(token, Opcodes.ACC_PRIVATE or Opcodes.ACC_STATIC, classNode.type, JavaMethod.STATIC_INITIALIZATION_BLOCK,
  FunctionBlockNode(token, scope, mutableListOf())
  , mutableListOf(), JavaType.void,
  scope, false, false) {

  companion object {

    fun newInstance(classNode: ClassNode): StaticInitializationNode {
      val scope = MethodScope(classNode.scope, JavaMethod.STATIC_INITIALIZATION_BLOCK, emptyList(), JavaType.void)
      return StaticInitializationNode(classNode.token, classNode, scope)
    }
  }
}