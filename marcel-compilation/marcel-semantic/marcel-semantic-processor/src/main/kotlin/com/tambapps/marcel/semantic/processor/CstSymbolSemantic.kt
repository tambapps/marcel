package com.tambapps.marcel.semantic.processor

import com.tambapps.marcel.parser.cst.ConstructorCstNode
import com.tambapps.marcel.parser.cst.FieldCstNode
import com.tambapps.marcel.parser.cst.MethodCstNode
import com.tambapps.marcel.parser.cst.MethodParameterCstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.symbol.method.JavaConstructorImpl
import com.tambapps.marcel.semantic.symbol.method.MarcelMethod
import com.tambapps.marcel.semantic.symbol.method.MarcelMethodImpl
import com.tambapps.marcel.semantic.symbol.method.MethodParameter
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.variable.field.JavaClassFieldImpl
import com.tambapps.marcel.semantic.symbol.variable.field.MarcelField

/**
 * Interface providing util methods to generate semantic objects from CST members
 */
interface CstSymbolSemantic {

  fun toMarcelField(ownerType: JavaType, fieldNode: FieldCstNode): MarcelField {
    return JavaClassFieldImpl(
      resolve(fieldNode.type), fieldNode.name, ownerType, fieldNode.access.isFinal,
      Visibility.fromTokenType(fieldNode.access.visibility), fieldNode.access.isStatic
    )
  }

  fun toJavaMethod(ownerType: JavaType, forExtensionType: JavaType? = null, node: MethodCstNode): MarcelMethod {
    val visibility = Visibility.fromTokenType(node.accessNode.visibility)
    val isStatic = node.accessNode.isStatic
    val (returnType, asyncReturnType) = resolveReturnType(node)
    return MarcelMethodImpl(
      ownerType, Visibility.fromTokenType(node.accessNode.visibility), node.name,
      node.parameters.mapIndexed { index, methodParameterCstNode ->
        toMethodParameter(
          ownerType,
          forExtensionType,
          visibility,
          isStatic,
          index,
          node.name,
          methodParameterCstNode
        )
      },
      returnType, isDefault = false, isAbstract = false, isStatic = isStatic, isConstructor = false,
      isAsync = node.isAsync, asyncReturnType = asyncReturnType, isVarArgs = node.isVarArgs
    )
  }

  fun toJavaConstructor(ownerType: JavaType, node: ConstructorCstNode): MarcelMethod {
    val visibility = Visibility.fromTokenType(node.accessNode.visibility)
    return JavaConstructorImpl(
      visibility,
      isVarArgs = node.isVarArgs,
      isSynthetic = false,
      ownerType,
      node.parameters.mapIndexed { index, methodParameterCstNode ->
        toMethodParameter(
          ownerType,
          null,
          visibility,
          false,
          index,
          "constructor",
          methodParameterCstNode
        )
      })
  }

  fun toMethodParameter(
    ownerType: JavaType, forExtensionType: JavaType?, visibility: Visibility,
    isStatic: Boolean, parameterIndex: Int,
    methodName: String, node: MethodParameterCstNode
  ): MethodParameter

  fun resolve(node: TypeCstNode): JavaType

  // returnType, asyncReturnType
  fun resolveReturnType(node: MethodCstNode): Pair<JavaType, JavaType?> {
    val type = resolve(node.returnTypeNode)
    return if (!node.isAsync) type to null
    else JavaType.Future.withGenericTypes(type.objectType) to
        if (type == JavaType.void) JavaType.void else type.objectType
  }

}