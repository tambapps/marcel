package com.tambapps.marcel.semantic

import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.semantic.ast.Ast2Node
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.MethodParameterNode
import com.tambapps.marcel.semantic.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.semantic.ast.expression.literal.NullValueNode
import com.tambapps.marcel.semantic.ast.expression.literal.VoidExpressionNode
import com.tambapps.marcel.semantic.ast.statement.BlockStatementNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver

internal object SemanticHelper {

  fun staticInitialisationMethod(classNode: ClassNode): MethodNode {
    return MethodNode(
      ownerClass = classNode.type,
      name = JavaMethod.STATIC_INITIALIZATION_BLOCK,
      parameters = emptyList(),
      visibility = Visibility.PRIVATE,
      isStatic = true,
      returnType = JavaType.void,
      tokenStart = classNode.tokenStart,
      tokenEnd = classNode.tokenEnd
    ).apply {
      blockStatement = BlockStatementNode(mutableListOf(), tokenStart, tokenEnd)
      // all functions should finish with a return statement, even the void ones
      blockStatement.statements.add(ReturnStatementNode(null, tokenStart, tokenEnd))
    }
  }

  fun noArgConstructor(classNode: ClassNode, typeResolver: JavaTypeResolver): MethodNode {
    val defaultConstructorNode = MethodNode(JavaMethod.CONSTRUCTOR_NAME, emptyList(),  Visibility.PUBLIC, JavaType.void, false, classNode.tokenStart, classNode.tokenEnd, JavaType.void)
    val superConstructorMethod = typeResolver.findMethodOrThrow(classNode.superType, JavaMethod.CONSTRUCTOR_NAME, emptyList(), classNode.token)
    defaultConstructorNode.blockStatement = BlockStatementNode(mutableListOf(
      ExpressionStatementNode(SuperConstructorCallNode(classNode.superType, superConstructorMethod, emptyList(), defaultConstructorNode.tokenStart, defaultConstructorNode.tokenEnd)),
      ReturnStatementNode(VoidExpressionNode(defaultConstructorNode.token), defaultConstructorNode.tokenStart, defaultConstructorNode.tokenEnd)
    ), defaultConstructorNode.tokenStart, defaultConstructorNode.tokenEnd)
    return defaultConstructorNode
  }

  fun returnVoid(node: Ast2Node) = ReturnStatementNode(VoidExpressionNode(node.token), node.tokenStart, node.tokenEnd)
  fun returnNull(node: Ast2Node) = ReturnStatementNode(NullValueNode(node.token), node.tokenStart, node.tokenEnd)

  fun scriptRunMethod(classType: JavaType, cst: SourceFileCstNode) = MethodNode(name = "run",
    visibility = Visibility.PUBLIC, returnType = JavaType.Object,
    isStatic = false,
    ownerClass = classType,
    parameters = listOf(MethodParameterNode(cst.tokenStart, JavaType.String.arrayType, "args")),
    tokenStart = cst.tokenStart,
    tokenEnd = cst.tokenEnd)
}