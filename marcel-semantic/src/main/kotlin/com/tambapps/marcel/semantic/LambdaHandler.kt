package com.tambapps.marcel.semantic

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.LambdaCstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.LambdaClassNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.expression.NewInstanceNode
import com.tambapps.marcel.semantic.ast.expression.NewLambdaInstanceNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.method.MethodParameter
import com.tambapps.marcel.semantic.scope.ClassScope
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

  fun predefineLambda(node: LambdaCstNode,
                      parameters: List<LambdaClassNode.MethodParameter>,
                      smartCastType: JavaType?, currentMethodScope: MethodScope): NewInstanceNode {
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

    val lambdaNode = LambdaClassNode(lambdaType, lambdaConstructor, node, parameters).apply {
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

  fun defineLambda(lambdaNode: LambdaClassNode, lambdaClassScope: ClassScope) {
    val interfaceType =
      if (lambdaNode.interfaceTypes.size == 1
        && !Lambda::class.javaType.isAssignableFrom(lambdaNode.interfaceTypes.first())) lambdaNode.interfaceTypes.first()
    else if (lambdaNode.interfaceTypes.isEmpty()) null
    else throw MarcelSemanticException(lambdaNode.token, "Expected lambda to be of multiple types: " + lambdaNode.interfaceTypes)

    val methodParameters = computeLambdaParameters(lambdaNode, interfaceType)


    TODO("Not yet implemented")
  }

  private fun computeLambdaParameters(lambdaNode: LambdaClassNode, interfaceType: JavaType?): List<MethodParameter> {
    if (interfaceType == null) {
      return if (lambdaNode.explicit0Parameters) emptyList()
      else if (lambdaNode.lambdaMethodParameters.isEmpty()) listOf(MethodParameter(JavaType.Object, "it"))
      else lambdaNode.lambdaMethodParameters.map { MethodParameter(it.type ?: JavaType.Object, it.name) }
    }
    val method = typeResolver.getInterfaceLambdaMethod(interfaceType)

    if (lambdaNode.explicit0Parameters) {
      if (method.parameters.isNotEmpty()) throw MarcelSemanticException(lambdaNode.token, "Lambda parameters mismatch. Expected parameters ${method.parameters}")
      return emptyList()
    }
    if (lambdaNode.lambdaMethodParameters.isEmpty()) {
      if (method.parameters.size > 1) throw MarcelSemanticException(lambdaNode.token, "Lambda parameters mismatch. Expected parameters ${method.parameters}")
      return method.parameters.map { MethodParameter(it.type, "it") }
    }

    if (lambdaNode.lambdaMethodParameters.size != method.parameters.size) {
      throw MarcelSemanticException(lambdaNode.token, "Lambda parameters mismatch. Expected parameters ${method.parameters}")
    }
    return lambdaNode.lambdaMethodParameters.mapIndexed { index, lambdaMethodParameter ->
      MethodParameter(lambdaMethodParameter.type ?: method.parameters[index].type, lambdaMethodParameter.name)
    }
  }
}