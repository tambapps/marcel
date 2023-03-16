package com.tambapps.marcel.parser.type

import com.tambapps.marcel.parser.ast.MethodParameter
import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.ast.AstNodeTypeResolver
import com.tambapps.marcel.parser.ast.AstTypedObject
import com.tambapps.marcel.parser.ast.expression.CharConstantNode
import com.tambapps.marcel.parser.ast.expression.DoubleConstantNode
import com.tambapps.marcel.parser.ast.expression.FloatConstantNode
import com.tambapps.marcel.parser.ast.expression.IntConstantNode
import com.tambapps.marcel.parser.ast.expression.LongConstantNode
import com.tambapps.marcel.parser.ast.expression.NullValueNode
import com.tambapps.marcel.parser.ast.expression.StringConstantNode
import com.tambapps.marcel.parser.scope.Scope
import marcel.lang.DefaultValue
import org.objectweb.asm.Opcodes
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Parameter
import java.lang.reflect.ParameterizedType
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType
import java.util.*

interface JavaMethod {

  companion object {
    const val CONSTRUCTOR_NAME = "<init>"
    const val STATIC_INITIALIZATION_BLOCK = "<clinit>"
  }

  val ownerClass: JavaType
  val access: Int
  val visibility: Visibility
  val name: String
  val parameters: List<MethodParameter>
  val returnType: JavaType
  // for generic methods
  val actualReturnType: JavaType
  val descriptor: String
  val signature: String
    get() {
      val builder = StringBuilder()
      // using rawType because these are the one used in compiled classes
      parameters.joinTo(buffer = builder, separator = "", transform = { it.type.descriptor }, prefix = "(", postfix = ")")
      builder.append(returnType.descriptor)
      return builder.toString()
    }
  val isDefault: Boolean
  val isAbstract: Boolean

  val isStatic: Boolean
    get() = (access and Opcodes.ACC_STATIC) != 0
  val isConstructor: Boolean
  val isInline: Boolean get() = false
  val invokeCode: Int
    get() = if (isStatic) Opcodes.INVOKESTATIC
    else if (ownerClass.isInterface) Opcodes.INVOKEINTERFACE
    else Opcodes.INVOKEVIRTUAL

  fun isAccessibleFrom(scope: Scope): Boolean {
    return visibility.canAccess(scope.classType, ownerClass)
  }

  fun parameterMatches(other: JavaMethod): Boolean {
    if (parameters.size != other.parameters.size) return false
    for (i in parameters.indices) if (parameters[i].type != other.parameters[i].type) return false
    return true
  }

  fun matches(other: JavaMethod): Boolean {
    if (name != other.name) return false
    if (!parameterMatches(other)) return false
    if (returnType != other.returnType) return false
    return true
  }
  fun matchesUnorderedParameters(typeResolver: AstNodeTypeResolver, name: String,
                                 positionalArguments: List<AstTypedObject>,
                                 arguments: Collection<MethodParameter>): Boolean {
    if (positionalArguments.isNotEmpty()) {
      if (positionalArguments.size > this.parameters.size || positionalArguments.size + arguments.size > this.parameters.size) return false
      for (i in positionalArguments.indices) {
        if (!this.parameters[i].type.isAssignableFrom(positionalArguments[i].type)) {
          return false
        }
      }
    }
    val methodParameters = this.parameters.subList(positionalArguments.size, this.parameters.size)
    if (this.name != name) return false
    if (arguments.size > methodParameters.size || arguments.any { p -> methodParameters.none { it.name == p.name } }) return false
    for (methodParameter in methodParameters) {
      if (arguments.none { methodParameter.type.isAssignableFrom(it.type) && it.name == methodParameter.name } && methodParameter.defaultValue == null) {
        return false
      }
    }
    return true
  }

  fun matches(typeResolver: AstNodeTypeResolver, name: String, types: List<AstTypedObject>): Boolean {
    return this.name == name && matches(typeResolver, types)
  }

  fun matches(typeResolver: AstNodeTypeResolver, argumentTypes: List<AstTypedObject>): Boolean {
    if (argumentTypes.size > parameters.size) return false
    var i = 0
    while (i < argumentTypes.size) {
      val expectedType = parameters[i].type
      val actualType = argumentTypes[i].type
      if (!matches(typeResolver, expectedType, actualType)) return false
      i++
    }

    // if all remaining parameters have default value, this is a valid function call
    while (i < parameters.size) {
      if (!parameters[i].hasDefaultValue) return false
      i++
    }
    return true
  }

  fun exactMatch(name: String, types: List<AstTypedObject>): Boolean {
    return this.name == name && this.parameters.map { it.type } == types.map { it.type }
  }

  private fun matches(typeResolver: AstNodeTypeResolver, expectedType: JavaType, actualType: JavaType): Boolean {
    return if (expectedType.isInterface && actualType.isLambda) {
      val declaredMethods = typeResolver.getDeclaredMethods(expectedType)
        .filter { it.isAbstract }
      if (declaredMethods.size != 1) return false
      val interfaceMethod = declaredMethods.first()
      val lambdaMethod = typeResolver.getInterfaceLambdaMethod(actualType)
      return interfaceMethod.parameters.size == lambdaMethod.parameters.size // there's probably if there's a better way for that
    //return interfaceMethod.matches(typeResolver, lambdaMethod.parameters)
    } else expectedType.isAssignableFrom(actualType)
  }

  fun withGenericTypes(types: List<JavaType>): JavaMethod {
    // this is especially for ExtensionMethod, which don't have generic actual parameters since the type was gotten
    // from the first method's parameter
    return this
  }
}
// see norm of modifiers flag in Modifier class. Seems to have the same norm as OpCodes.ACC_ modifiers
abstract class AbstractMethod constructor(final override val access: Int): JavaMethod {

  override val visibility = Visibility.fromAccess(access)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is JavaMethod) return false
    if (name != other.name) return false
    if (parameters != other.parameters) return false
    if (returnType != other.returnType) return false
    return true
  }

  val isGetter get() = name.length > 3 && name.startsWith("get") && name[3].isUpperCase()
  val isSetter get() = name.length > 3 && name.startsWith("set") && name[3].isUpperCase()

  override fun hashCode(): Int {
    return Objects.hash(name, parameters, returnType)
  }
}

class ReflectJavaConstructor(constructor: Constructor<*>): AbstractMethod(constructor.modifiers) {
  override val ownerClass = JavaType.of(constructor.declaringClass)

  // see norm of modifiers flag in Modifier class. Seems to have the same norm as OpCodes.ACC_ modifiers
  override val name: String = JavaMethod.CONSTRUCTOR_NAME
  override val parameters = constructor.parameters.map { MethodParameter(JavaType.of(it.type), it.name) }
  override val returnType = JavaType.void // yes, constructor returns void, especially for the descriptor
  override val actualReturnType = returnType
  override val descriptor = AsmUtils.getMethodDescriptor(parameters, returnType)
  override val invokeCode = Opcodes.INVOKESPECIAL
  override val isConstructor = true
  override val isDefault = false
  override val isAbstract = false

  override fun toString(): String {
    return "${ownerClass.className}(" + parameters.joinToString(separator = ", ", transform = { "${it.type} ${it.name}"}) + ") " + returnType
  }
}
class ExtensionJavaMethod(
    private val reflectMethod: Method,
    override val ownerClass: JavaType,
    override val name: String,
    override val parameters: List<MethodParameter>,
    override val returnType: JavaType,
    override val actualReturnType: JavaType,
    override val descriptor: String,
) : AbstractMethod(Opcodes.ACC_PUBLIC) {
  override val isConstructor = false
  // the static is excluded here in purpose so that self is pushed to the stack
  override val invokeCode = Opcodes.INVOKESTATIC
  override val isAbstract = false
  override val isDefault = false

  // TODO could probably do some optimization here (with uses of java reflect API)
  constructor(method: Method): this(method,
    JavaType.of(method.declaringClass), method.name,
    method.parameters.takeLast(method.parameters.size - 1).map { ReflectJavaMethod.methodParameter(
      JavaType.of(method.parameters.first().type),
      it
    ) },
    JavaType.of(method.returnType),
    ReflectJavaMethod.actualMethodReturnType(JavaType.of(method.parameters.first().type), method, true),
    AsmUtils.getDescriptor(method))

  override fun withGenericTypes(types: List<JavaType>): JavaMethod {
    val actualOwnerClass = JavaType.of(reflectMethod.parameters.first().type).withGenericTypes(types)
    return ExtensionJavaMethod(reflectMethod, ownerClass, name,
      reflectMethod.parameters.takeLast(reflectMethod.parameters.size - 1).map { ReflectJavaMethod.methodParameter(
        actualOwnerClass,
        it
      ) },
      returnType,
      ReflectJavaMethod.actualMethodReturnType(actualOwnerClass, reflectMethod, true)
      , descriptor)
  }
  override fun toString(): String {
    return "$ownerClass.$name(" + parameters.joinToString(separator = ", ", transform = { "${it.type} ${it.name}"}) + ") " + returnType
  }
}
class ReflectJavaMethod constructor(method: Method, fromType: JavaType?): AbstractMethod(method.modifiers) {

  constructor(method: Method): this(method, null)

  override val ownerClass = JavaType.of(method.declaringClass)

  override val name: String = method.name
  override val parameters = method.parameters.map { methodParameter(fromType, it) }
  override val returnType = JavaType.of(method.returnType)
  override val actualReturnType = actualMethodReturnType(fromType, method)
  override val descriptor = AsmUtils.getMethodDescriptor(parameters, returnType)
  override val isConstructor = false
  override val isAbstract = (method.modifiers and Modifier.ABSTRACT) != 0
  override val isDefault = method.isDefault

  override fun toString(): String {
    return "$ownerClass.$name(" + parameters.joinToString(separator = ", ", transform = { "${it.type} ${it.name}"}) + ") " + returnType
  }

  companion object {

    internal fun methodParameter(fromType: JavaType?, parameter: Parameter): MethodParameter {
      val type = methodParameterType(fromType, parameter)
      val rawType = JavaType.of(parameter.type)
      val annotations = parameter.annotations
      val defaultValueAnnotation = annotations.firstNotNullOfOrNull { it as? DefaultValue }
      val defaultValue = if (defaultValueAnnotation != null) {
        when (rawType) {
          JavaType.int, JavaType.Integer -> IntConstantNode(value = defaultValueAnnotation.defaultIntValue)
          JavaType.long, JavaType.Long -> LongConstantNode(value = defaultValueAnnotation.defaultLongValue)
          JavaType.float, JavaType.Float -> FloatConstantNode(value = defaultValueAnnotation.defaultFloatValue)
          JavaType.double, JavaType.Double -> DoubleConstantNode(value = defaultValueAnnotation.defaultDoubleValue)
          JavaType.char, JavaType.Character -> CharConstantNode(value = defaultValueAnnotation.defaultCharValue + "")
          JavaType.String -> StringConstantNode(value = defaultValueAnnotation.defaultStringValue)
          else -> NullValueNode()
        }
      } else null
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
      val parameterNames = javaType.genericParameterNames
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

              else -> TODO("Sounds difficult to implement")
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