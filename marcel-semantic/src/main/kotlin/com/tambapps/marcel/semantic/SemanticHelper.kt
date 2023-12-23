package com.tambapps.marcel.semantic

import com.tambapps.marcel.parser.cst.SourceFileNode
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.semantic.ast.expression.literal.NullValueNode
import com.tambapps.marcel.semantic.ast.expression.literal.VoidExpressionNode
import com.tambapps.marcel.semantic.ast.statement.BlockStatementNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.method.MethodParameter
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import com.tambapps.marcel.semantic.variable.LocalVariable
import marcel.lang.Binding
import marcel.lang.lambda.*

object SemanticHelper {

  fun staticInitialisationMethod(classNode: ClassNode): MethodNode {
    return MethodNode(
      ownerClass = classNode.type,
      name = JavaMethod.STATIC_INITIALIZATION_BLOCK,
      parameters = mutableListOf(),
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

  fun scriptBindingConstructor(classNode: ClassNode, typeResolver: JavaTypeResolver, scriptType: JavaType): MethodNode {
    val parameter = MethodParameter(Binding::class.javaType, "binding")
    val methodNode = MethodNode(JavaMethod.CONSTRUCTOR_NAME, mutableListOf(parameter),  Visibility.PUBLIC, JavaType.void, false, classNode.tokenStart, classNode.tokenEnd, JavaType.void)
    methodNode.blockStatement = BlockStatementNode(mutableListOf(
      ExpressionStatementNode(

        SuperConstructorCallNode(classNode.superType,
          typeResolver.findMethod(scriptType, JavaMethod.CONSTRUCTOR_NAME, listOf(parameter))!!,
          listOf(ReferenceNode(variable = LocalVariable(parameter.type, parameter.name, parameter.type.nbSlots, 1, false), token = classNode.token)), classNode.tokenStart, classNode.tokenEnd)
      ),
      ReturnStatementNode(VoidExpressionNode(methodNode.token), methodNode.tokenStart, methodNode.tokenEnd)
    ), methodNode.tokenStart, methodNode.tokenEnd)
    return methodNode
  }

  fun noArgConstructor(classNode: ClassNode, typeResolver: JavaTypeResolver, visibility: Visibility = Visibility.PUBLIC): MethodNode {
    val defaultConstructorNode = MethodNode(JavaMethod.CONSTRUCTOR_NAME, mutableListOf(),  visibility, JavaType.void, false, classNode.tokenStart, classNode.tokenEnd, JavaType.void)
    defaultConstructorNode.blockStatement = BlockStatementNode(mutableListOf(
      ExpressionStatementNode(superNoArgConstructorCall(classNode, typeResolver)),
      ReturnStatementNode(VoidExpressionNode(defaultConstructorNode.token), defaultConstructorNode.tokenStart, defaultConstructorNode.tokenEnd)
    ), defaultConstructorNode.tokenStart, defaultConstructorNode.tokenEnd)
    return defaultConstructorNode
  }

  fun superNoArgConstructorCall(classNode: ClassNode, typeResolver: JavaTypeResolver): SuperConstructorCallNode {
    val superConstructorMethod = typeResolver.findMethodOrThrow(classNode.superType, JavaMethod.CONSTRUCTOR_NAME, emptyList(), classNode.token)
    return SuperConstructorCallNode(classNode.superType, superConstructorMethod, emptyList(), classNode.tokenStart, classNode.tokenEnd)
  }

  fun returnVoid(node: AstNode) = ReturnStatementNode(VoidExpressionNode(node.token), node.tokenStart, node.tokenEnd)
  fun returnNull(node: AstNode) = ReturnStatementNode(NullValueNode(node.token), node.tokenStart, node.tokenEnd)

  fun parameterToLocalVariable(method: JavaMethod, parameter: MethodParameter): LocalVariable {
    var index = if (method.isStatic) 0 else 1
    var i = 0
    while (i < method.parameters.size && method.parameters[i] != parameter) {
      index+= method.parameters[i++].type.nbSlots
    }
    return LocalVariable(parameter.type, parameter.name, parameter.type.nbSlots, index, parameter.isFinal)
  }

  fun scriptRunMethod(classType: JavaType, cst: SourceFileNode) = MethodNode(name = "run",
    visibility = Visibility.PUBLIC, returnType = JavaType.Object,
    isStatic = false,
    ownerClass = classType,
    parameters = mutableListOf(MethodParameter(JavaType.String.arrayType, "args")),
    tokenStart = cst.tokenStart,
    tokenEnd = cst.tokenEnd)


  fun getLambdaType(node: AstNode, lambdaParameters: List<MethodParameter>): JavaType {
    val returnType = JavaType.Object

    return when (lambdaParameters.size) {
      0 -> JavaType.of(Lambda1::class.java).withGenericTypes(JavaType.Object)
      1 -> when(lambdaParameters.first().type) {
        JavaType.int -> JavaType.of(IntLambda1::class.java).withGenericTypes(returnType)
        JavaType.long -> JavaType.of(LongLambda1::class.java).withGenericTypes(returnType)
        JavaType.float -> JavaType.of(FloatLambda1::class.java).withGenericTypes(returnType)
        JavaType.double -> JavaType.of(DoubleLambda1::class.java).withGenericTypes(returnType)
        JavaType.char -> JavaType.of(CharLambda1::class.java).withGenericTypes(returnType)
        else -> JavaType.of(Lambda1::class.java).withGenericTypes(lambdaParameters.first().type.objectType, returnType)
      }
      2 -> JavaType.of(Lambda2::class.java).withGenericTypes(lambdaParameters.map { it.type } + returnType)
      3 -> JavaType.of(Lambda3::class.java).withGenericTypes(lambdaParameters.map { it.type } + returnType)
      4 -> JavaType.of(Lambda4::class.java).withGenericTypes(lambdaParameters.map { it.type } + returnType)
      5 -> JavaType.of(Lambda5::class.java).withGenericTypes(lambdaParameters.map { it.type } + returnType)
      6 -> JavaType.of(Lambda6::class.java).withGenericTypes(lambdaParameters.map { it.type } + returnType)
      7 -> JavaType.of(Lambda7::class.java).withGenericTypes(lambdaParameters.map { it.type } + returnType)
      8 -> JavaType.of(Lambda8::class.java).withGenericTypes(lambdaParameters.map { it.type } + returnType)
      9 -> JavaType.of(Lambda9::class.java).withGenericTypes(lambdaParameters.map { it.type } + returnType)
      10 -> JavaType.of(Lambda10::class.java).withGenericTypes(lambdaParameters.map { it.type } + returnType)
      else -> throw MarcelSemanticException(node.token, "Doesn't handle lambdas with more than 10 parameters")
    }
  }

  // add the statement last, but before the return instruction if any
  fun addStatementLast(statement: StatementNode, block: BlockStatementNode) {
    addStatementLast(statement, block.statements)
  }

  private fun addStatementLast(statement: StatementNode, statements: MutableList<StatementNode>) {
    if (statements.last() is ReturnStatementNode) {
      statements.add(statements.lastIndex, statement)
    } else statements.add(statement)
  }
}