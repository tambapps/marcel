package com.tambapps.marcel.semantic.method

import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.type.JavaType
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Parameter
import java.lang.reflect.ParameterizedType
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType

class ReflectJavaMethod constructor(method: Method, fromType: JavaType?): AbstractMethod() {

  constructor(method: Method): this(method, null)

  override val ownerClass = JavaType.of(method.declaringClass)

  override val name: String = method.name
  override val visibility = Visibility.fromAccess(method.modifiers)
  override val parameters = method.parameters.map { methodParameter(ownerClass, fromType, it) }
  override val returnType = JavaType.of(method.returnType)
  override val actualReturnType = actualMethodReturnType(fromType, method)
  override val isConstructor = false
  override val isAbstract = (method.modifiers and Modifier.ABSTRACT) != 0
  override val isDefault = method.isDefault
  override val isStatic = (method.modifiers and Modifier.STATIC) != 0

  override fun toString(): String {
    return "$ownerClass.$name(" + parameters.joinToString(separator = ", ", transform = { "${it.type} ${it.name}"}) + ") " + returnType
  }

  companion object {

    internal fun methodParameter(ownerType: JavaType, fromType: JavaType?, parameter: Parameter): MethodParameter {
      val type = methodParameterType(fromType, parameter)
      val rawType = JavaType.of(parameter.type)
      val annotations = parameter.annotations
      val defaultValue: ExpressionNode? = when {
        // TODO
        else -> null
      }
      return MethodParameter(type, rawType, parameter.name, (parameter.modifiers and Modifier.FINAL) != 0, defaultValue)
    }

    internal fun actualMethodReturnType(fromType: JavaType?, method: Method, extensionMethod: Boolean = false): JavaType {
      if (fromType == null) return JavaType.of(method.returnType)
      val genericReturnType = method.genericReturnType
      if (genericReturnType.typeName == method.returnType.typeName) return JavaType.of(method.returnType)
      if (genericReturnType is ParameterizedType && genericReturnType.actualTypeArguments.all { it is Class<*> }) {
        // if all generic types are already set in method definition, just use them
        return JavaType.of(method.returnType).withGenericTypes(genericReturnType.actualTypeArguments.map { JavaType.of(it as Class<*>) })
        }
      // type is generic? let's get it from here
      val typeParameters = fromType.realClazz.typeParameters
      var typeParameterIndex = typeParameters.indexOfFirst { it.name == genericReturnType.typeName }
      if (typeParameterIndex >= 0) return fromType.genericTypes.getOrNull(typeParameterIndex) ?: JavaType.of(method.returnType)
      if (extensionMethod) {
        val realOwnerType = method.parameters.first().parameterizedType as? ParameterizedType
        if (realOwnerType != null) {
          typeParameterIndex = realOwnerType.actualTypeArguments.indexOfFirst { it.typeName == genericReturnType.typeName }
          if (typeParameterIndex >= 0) return fromType.genericTypes.getOrNull(typeParameterIndex) ?: JavaType.of(method.returnType)
        }
      }
      return JavaType.of(method.returnType)
    }

    private fun methodParameterType(javaType: JavaType?, methodParameter: Parameter): JavaType {
      val rawType = JavaType.of(methodParameter.type)
      if (javaType == null || javaType.genericTypes.isEmpty()) return rawType
      val parameterizedType = methodParameter.parameterizedType
      val parameterNames = javaType.realClazz.typeParameters.map { it.name }
      when (parameterizedType) {
        is ParameterizedType -> {
          val genericTypes = parameterizedType.actualTypeArguments.map {
            when (it) {
              is WildcardType -> {
                var index = parameterNames.indexOf(it.upperBounds.first().typeName)
                if (index < 0) index = parameterNames.indexOf(it.lowerBounds.first().typeName)
                return@map javaType.genericTypes.getOrNull(index) ?: JavaType.Object
              }

              is TypeVariable<*> -> {
                val index = parameterizedType.actualTypeArguments.indexOfFirst { at -> at.typeName == it.typeName }
                return@map javaType.genericTypes.getOrNull(index) ?: JavaType.Object
              }

              else -> rawType // sounds difficult to implement. Marcel isn't supposed to handle generic types anyway
            }
          }
          return rawType.withGenericTypes(genericTypes)
        }

        is TypeVariable<*> -> {
          val index = parameterNames.indexOf(parameterizedType.name)
          return javaType.genericTypes.getOrNull(index) ?: rawType
        }

        else -> return rawType
      }
    }
  }
}
