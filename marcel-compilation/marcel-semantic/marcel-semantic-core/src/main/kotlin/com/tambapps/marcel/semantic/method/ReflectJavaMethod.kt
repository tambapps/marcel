package com.tambapps.marcel.semantic.method

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.FunctionCallNode
import com.tambapps.marcel.semantic.ast.expression.literal.BoolConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.CharConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.DoubleConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.FloatConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.IntConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.LongConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.NullValueNode
import com.tambapps.marcel.semantic.type.JavaType
import marcel.lang.compile.BooleanDefaultValue
import marcel.lang.compile.CharDefaultValue
import marcel.lang.compile.DoubleDefaultValue
import marcel.lang.compile.FloatDefaultValue
import marcel.lang.compile.IntDefaultValue
import marcel.lang.compile.LongDefaultValue
import marcel.lang.compile.MethodCallDefaultValue
import marcel.lang.compile.NullDefaultValue
import marcel.util.concurrent.Async
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
  override val parameters = method.parameters.map { methodParameter(method.name, ownerClass, fromType, it) }
  override val returnType = JavaType.of(method.returnType)
  override val actualReturnType = actualMethodReturnType(fromType, method)
  override val isConstructor = false
  override val isAbstract = (method.modifiers and Modifier.ABSTRACT) != 0
  override val isDefault = method.isDefault
  override val isStatic = (method.modifiers and Modifier.STATIC) != 0
  override val isVarArgs = method.isVarArgs
  override val asyncReturnType = method.getAnnotation(Async::class.java)?.returnType?.let { JavaType.of(it.java) }

  companion object {

    internal fun methodParameter(methodName: String, ownerType: JavaType, fromType: JavaType?, parameter: Parameter): MethodParameter {
      val type = methodParameterType(fromType, parameter)
      val rawType = JavaType.of(parameter.type)
      val annotations = parameter.annotations
      val defaultValue: ExpressionNode? = when {
        annotations.any { it is NullDefaultValue } -> NullValueNode(LexToken.DUMMY)
        type == JavaType.int || type == JavaType.Integer -> annotations.firstNotNullOfOrNull { it as? IntDefaultValue }?.let {
          IntConstantNode(value = it.value, token = LexToken.DUMMY)
        }
        type == JavaType.long || type == JavaType.Long -> annotations.firstNotNullOfOrNull { it as? LongDefaultValue }?.let {
          LongConstantNode(value = it.value, token = LexToken.DUMMY)
        }
        type == JavaType.float || type == JavaType.Float -> annotations.firstNotNullOfOrNull { it as? FloatDefaultValue }?.let {
          FloatConstantNode(value = it.value, token = LexToken.DUMMY)
        }
        type == JavaType.double || type == JavaType.Double -> annotations.firstNotNullOfOrNull { it as? DoubleDefaultValue }?.let {
          DoubleConstantNode(value = it.value, token = LexToken.DUMMY)
        }
        type == JavaType.char || type == JavaType.Character -> annotations.firstNotNullOfOrNull { it as? CharDefaultValue }?.let {
          CharConstantNode(value = it.value, token = LexToken.DUMMY)
        }
        type == JavaType.boolean || type == JavaType.Boolean -> annotations.firstNotNullOfOrNull { it as? BooleanDefaultValue }?.let {
          BoolConstantNode(value = it.value, token = LexToken.DUMMY)
        }
        annotations.any { it is MethodCallDefaultValue } -> {
          val defaultValueMethodName = annotations.firstNotNullOfOrNull { it as? MethodCallDefaultValue }!!.methodName
          // using reflect to search method and not type resolver in order to avoid infinite recursion
          val match = try {
            // check to avoid infinite recursion
            if (defaultValueMethodName != methodName) ReflectJavaMethod(ownerType.realClazz.getDeclaredMethod(defaultValueMethodName))
            else null
          } catch (e: NoSuchMethodException) { null }

          if (match != null && type.isAssignableFrom(match.returnType)
            && match.isStatic
            && match.parameters.isEmpty()) FunctionCallNode(match, null, emptyList(), LexToken.DUMMY, LexToken.DUMMY)
          else null
        }
        else -> null
      }

      return MethodParameter(type, rawType, parameter.name, (parameter.modifiers and Modifier.FINAL) != 0, emptyList(),  defaultValue)
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
