package com.tambapps.marcel.semantic

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.LambdaCstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.LambdaClassNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.expression.NewInstanceNode
import com.tambapps.marcel.semantic.ast.expression.NewLambdaInstanceNode
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.scope.MethodScope
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import marcel.lang.lambda.Lambda

class LambdaHandler(
  private val typeResolver: JavaTypeResolver,
  // should be the same instance as the one from MarcelSemantic
  private val classNodeMap: Map<JavaType, ClassNode>
) {

  private val classLambdasMap = mutableMapOf<JavaType, LambdaClassNode>()

  fun predefineLambda(node: LambdaCstNode, smartCastType: JavaType?, currentMethodScope: MethodScope): NewInstanceNode {
    // search for already generated lambdaNode if not empty
    val lambdaClassName = generateLambdaName(node, currentMethodScope)
    val lambdaOuterClassNode = classNodeMap.getValue(currentMethodScope.classType)
    val alreadyExistingLambdaNode = lambdaOuterClassNode.innerClasses
      .find { it.type.simpleName == lambdaClassName } as? LambdaClassNode

    if (alreadyExistingLambdaNode != null) {
      return alreadyExistingLambdaNode.constructorCallNode
    }

    val interfaceType = if (smartCastType != null && !Lambda::class.javaType.isAssignableFrom(smartCastType)) smartCastType else null

    // other implemented interfaces are not handled now. They will be handled in another method
    val lambdaImplementedInterfaces = listOf(Lambda::class.javaType)
    val lambdaType = typeResolver.defineClass(node.token, Visibility.PRIVATE, lambdaOuterClassNode.type, lambdaClassName, JavaType.Object, false, lambdaImplementedInterfaces)

    val lambdaConstructor = MethodNode(
      name = JavaMethod.CONSTRUCTOR_NAME,
      visibility = Visibility.PRIVATE,
      returnType = JavaType.void,
      isStatic = false,
      tokenStart = node.tokenStart,
      tokenEnd = node.tokenEnd,
      parameters = mutableListOf(),
      ownerClass = lambdaType
    )

    val lambdaNode = LambdaClassNode(lambdaType, lambdaConstructor, node.tokenStart, node.tokenEnd).apply {
      interfaceType?.let { interfaceTypes.add(it) }
    }
    lambdaOuterClassNode.innerClasses.add(lambdaNode)

    val constructorCallNode = NewLambdaInstanceNode(lambdaNode.type, lambdaConstructor,
      // this part is important, as we will compute the constructorParameters later
      lambdaNode.constructorParameters, lambdaNode, node.token)
    lambdaNode.constructorCallNode = constructorCallNode
    return constructorCallNode
  }

  private fun generateLambdaName(node: CstNode, currentMethodScope: MethodScope): String {
    return "_lambda_" + node.hashCode().toString().replace('-', '0') + "_" + currentMethodScope.method.name
  }

  fun defineLambda(lambdaNode: LambdaClassNode) {
    TODO("Not yet implemented")
  }
}