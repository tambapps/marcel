package com.tambapps.marcel.semantic

import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.semantic.ast.expression.literal.VoidExpressionNode
import com.tambapps.marcel.semantic.ast.statement.BlockStatementNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver

internal object SemanticHelper {

  fun noArgConstructor(classNode: ClassNode, typeResolver: JavaTypeResolver): MethodNode {
    val defaultConstructorNode = MethodNode(JavaMethod.CONSTRUCTOR_NAME, Visibility.PUBLIC, JavaType.void, false, classNode.tokenStart, classNode.tokenEnd, JavaType.void)
    val superConstructorMethod = typeResolver.findMethodOrThrow(classNode.superType, JavaMethod.CONSTRUCTOR_NAME, emptyList(), classNode)
    defaultConstructorNode.blockStatement = BlockStatementNode(listOf(
      ExpressionStatementNode(SuperConstructorCallNode(classNode.superType, superConstructorMethod, emptyList(), defaultConstructorNode.tokenStart, defaultConstructorNode.tokenEnd)),
      ReturnStatementNode(VoidExpressionNode(defaultConstructorNode.token), defaultConstructorNode.tokenStart, defaultConstructorNode.tokenEnd)
    ), defaultConstructorNode.tokenStart, defaultConstructorNode.tokenEnd)
    return defaultConstructorNode
  }
}