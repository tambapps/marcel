package com.tambapps.marcel.parser.type

import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.MarcelParsingException
import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.ast.AstTypedObject
import com.tambapps.marcel.parser.exception.SemanticException
import org.objectweb.asm.Opcodes
import kotlin.reflect.KClass

interface JavaType: AstTypedObject {

  // whether the class is in the classpath and therefore can be accessed with Class.forName(className)
  val isLoaded: Boolean
  val realClazz: Class<*>
  val className: String
  val superClassName: String?
  val internalName
    get() = AsmUtils.getInternalName(className)
  val descriptor: String

  val storeCode: Int
  val loadCode: Int
  val returnCode: Int
  val genericTypes: List<JavaType>
  val isInterface: Boolean
  val primitive: Boolean
  override val type: JavaType get() = this
  val realClazzOrObject: Class<*>

  fun withGenericTypes(genericTypes: List<JavaType>): JavaType
  // return this type without generic types
  fun raw(): JavaType {
    return withGenericTypes(emptyList())
  }

  fun isAssignableFrom(other: JavaType): Boolean {
    if (genericTypes.size != other.genericTypes.size) return false
    for (i in genericTypes.indices) {
      if (!genericTypes[i].isAssignableFrom(other.genericTypes[i])) return false
    }
    if (this == other || this == Object && !other.primitive
      // to handle null values that can be cast to anything
      || !primitive && other == void) {
      return true
    }
    if (other.primitive && (
          this in listOf(of(java.lang.Object::class.java), of(Number::class.java), of((other as JavaPrimitiveType).objectClass))
        )) {
      return true
    }
    if (isLoaded && other.isLoaded) {
      return realClazz.isAssignableFrom(other.realClazz)
    } else {
      var otherSuperType = if (other.superClassName != null) of(other.superClassName!!) else null
      while (otherSuperType != null) {
        if (otherSuperType == this) return true
        otherSuperType = if (otherSuperType.superClassName != null) of(otherSuperType.superClassName!!) else null
      }
      return false
    }
  }

  fun defineMethod(method: JavaMethod)

  fun findMethod(name: String, argumentTypes: List<AstTypedObject>): JavaMethod?

  fun findConstructorOrThrow(argumentTypes: List<AstTypedObject>): JavaMethod {
    return findMethodOrThrow(JavaMethod.CONSTRUCTOR_NAME, argumentTypes)
  }

  fun findMethodOrThrow(name: String, argumentTypes: List<AstTypedObject>): JavaMethod {
    return findMethod(name, argumentTypes) ?: throw SemanticException("Method $name with parameters ${argumentTypes.map { it.type }} is not defined")
  }

  companion object {

    private val DEFINED_TYPES = mutableMapOf<String, JavaType>()

    fun defineClass(className: String, superClassName: String, isInterface: Boolean): JavaType {
      try {
        Class.forName(className)
        throw SemanticException("Class $className is already defined")
      } catch (e: ClassNotFoundException) {
        // ignore
      }
      if (DEFINED_TYPES.containsKey(className)) throw SemanticException("Class $className is already defined")
      val type = NotLoadedJavaType(className, emptyList(), superClassName, isInterface)
      DEFINED_TYPES[className] = type
      return type
    }

    fun of(clazz: Class<*>): JavaType {
      if (clazz.isPrimitive) {
        return PRIMITIVES.find { it.className == clazz.name } ?: throw RuntimeException("Compiler error. Primitive type $clazz is not being handled")
      }
      return of(clazz.name)
    }

    fun of(className: String): JavaType {
      if (PRIMITIVES.any { it.className == className })
      if (DEFINED_TYPES.containsKey(className)) return DEFINED_TYPES.getValue(className)
      try {
        val clazz = Class.forName(className)
        val type = LoadedObjectType(clazz)
        DEFINED_TYPES[className] = type
        return type
      } catch (e: ClassNotFoundException) {
        throw SemanticException("Class $className was not found")
      }
    }

    fun clear() {
      DEFINED_TYPES.clear()
    }

    val Object = LoadedObjectType(Object::class.java)
    val String = LoadedObjectType(String::class.java)
    val Boolean = LoadedObjectType(Class.forName("java.lang.Boolean"))
    val Integer = LoadedObjectType(Class.forName("java.lang.Integer"))
    val Long = LoadedObjectType(Class.forName("java.lang.Long"))
    val Float = LoadedObjectType(Class.forName("java.lang.Float"))
    val Double = LoadedObjectType(Class.forName("java.lang.Double"))

    val void = JavaPrimitiveType(java.lang.Void::class, Opcodes.ALOAD, Opcodes.ASTORE, Opcodes.RETURN, 0,0,0,0)
    val int = JavaPrimitiveType(java.lang.Integer::class, Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN, Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL, Opcodes.IDIV)
    val long = JavaPrimitiveType(java.lang.Long::class, Opcodes.LLOAD, Opcodes.LSTORE, Opcodes.LRETURN, Opcodes.LADD, Opcodes.LSUB, Opcodes.LMUL, Opcodes.LDIV)
    val float = JavaPrimitiveType(java.lang.Float::class, Opcodes.FLOAD, Opcodes.FSTORE, Opcodes.FRETURN, Opcodes.FADD, Opcodes.FSUB, Opcodes.FMUL, Opcodes.FDIV)
    val double = JavaPrimitiveType(java.lang.Double::class, Opcodes.DLOAD, Opcodes.DSTORE, Opcodes.DRETURN, Opcodes.DADD, Opcodes.DSUB, Opcodes.DMUL, Opcodes.DDIV)
    // apparently we use int instructions to store booleans
    val boolean = JavaPrimitiveType(java.lang.Boolean::class, Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN,  0,0,0,0)
    // Marcel doesn't support char, but we could still find char values from plain Java code
    val char = JavaPrimitiveType(java.lang.Character::class, Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN, Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL, Opcodes.IDIV)
    // TODO verify opcodes for these two below
    val byte = JavaPrimitiveType(java.lang.Byte::class, Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN, Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL, Opcodes.IDIV)
    val short = JavaPrimitiveType(java.lang.Short::class, Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN, Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL, Opcodes.IDIV)

    val PRIMITIVES = listOf(void, int, long, float, double, boolean, char, byte, short)

    val PRIMITIVE_CAST_INSTRUCTION_MAP = mapOf(
      Pair(Pair(int, long), Opcodes.I2L),
      Pair(Pair(int, float), Opcodes.I2F),
      Pair(Pair(int, double), Opcodes.I2D),
      Pair(Pair(int, boolean), Opcodes.I2B),
      Pair(Pair(long, int), Opcodes.L2I),
      Pair(Pair(long, float), Opcodes.L2F),
      Pair(Pair(long, double), Opcodes.L2D),
      Pair(Pair(float, int), Opcodes.F2I),
      Pair(Pair(float, long), Opcodes.F2L),
      Pair(Pair(float, double), Opcodes.F2D),
      Pair(Pair(double, int), Opcodes.D2I),
      Pair(Pair(double, long), Opcodes.D2L),
      Pair(Pair(double, float), Opcodes.D2F),
    )

    val TOKEN_TYPE_MAP = mapOf(
      Pair(TokenType.TYPE_INT, int),
      Pair(TokenType.TYPE_LONG, long),
      Pair(TokenType.TYPE_VOID, void),
      Pair(TokenType.TYPE_FLOAT, float),
      Pair(TokenType.TYPE_DOUBLE, double),
      Pair(TokenType.TYPE_BOOL, boolean),
    )
  }

}

abstract class AbstractJavaType: JavaType {

  override fun toString(): String {
    if (genericTypes.isNotEmpty()) {
      return className + "<" + genericTypes.joinToString(separator = ", ") + ">"
    }
    return className
  }

  override fun hashCode(): Int {
    var result = className.hashCode()
    result = 31 * result + genericTypes.hashCode()
    return result
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is JavaType) return false

    if (className != other.className) return false
    if (genericTypes != other.genericTypes) return false

    return true
  }

  private val methods = mutableListOf<JavaMethod>()

  override fun defineMethod(method: JavaMethod) {
    if (methods.any { it.matches(method.name, method.parameters) }) {
      throw SemanticException("Method with $method is already defined")
    }
    methods.add(method)
  }


  override fun findMethod(name: String, argumentTypes: List<AstTypedObject>): JavaMethod? {
    var m = methods.find { it.matches(name, argumentTypes) }
    if (m == null && isLoaded) {
      val clazz = type.realClazz
      if (name == JavaMethod.CONSTRUCTOR_NAME) {
        try {
          m = ReflectJavaConstructor(clazz.getDeclaredConstructor(*argumentTypes.map { it.type.realClazz }.toTypedArray()))
        } catch (e: NoSuchMethodException) {
          // ignored
        }
      } else {
        try {
          m = ReflectJavaMethod(clazz.getDeclaredMethod(name, *argumentTypes.map { it.type.realClazz }.toTypedArray()))
        } catch (e: NoSuchMethodException) {
          // ignored
        }
      }
    }
    return m
  }
}
class NotLoadedJavaType internal constructor(override val className: String, override val genericTypes: List<JavaType>, override val superClassName: String?, override val isInterface: Boolean): AbstractJavaType() {

  override val isLoaded = false
  override val realClazz: Class<*>
    get() = throw RuntimeException("Class $className is not loaded")
  override val primitive = false
  override val realClazzOrObject = java.lang.Object::class.java
  override val storeCode = Opcodes.ASTORE
  override val loadCode = Opcodes.ALOAD
  override val returnCode = Opcodes.ARETURN
  override val descriptor: String
    get() = AsmUtils.getObjectClassDescriptor(className)

  override fun withGenericTypes(genericTypes: List<JavaType>): JavaType {
    if (genericTypes.any { it.primitive }) {
      throw MarcelParsingException("Cannot have a primitive type as generic type")
    }
    return NotLoadedJavaType(className, genericTypes, superClassName, isInterface)
  }

}
abstract class LoadedJavaType internal constructor(final override val realClazz: Class<*>, final override val genericTypes: List<JavaType>,
                              override val storeCode: Int, override val loadCode: Int, override val returnCode: Int): AbstractJavaType() {
  override val isLoaded = true
  override val descriptor: String
    get() = AsmUtils.getClassDescriptor(realClazz)

  override val className: String = realClazz.name
  override val superClassName = realClazz.superclass?.name


  override val isInterface = realClazz.isInterface
  override val primitive = realClazz.isPrimitive
  override val realClazzOrObject = realClazz

  override fun toString(): String {
    if (genericTypes.isNotEmpty()) {
      return className + "<" + genericTypes.joinToString(separator = ", ") + ">"
    }
    return className
  }

  override fun hashCode(): Int {
    var result = className.hashCode()
    result = 31 * result + genericTypes.hashCode()
    return result
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is JavaType) return false

    if (className != other.className) return false
    if (genericTypes != other.genericTypes) return false

    return true
  }
}

class LoadedObjectType(
  realClazz: Class<*>,
  genericTypes: List<JavaType>,
): LoadedJavaType(realClazz, genericTypes, Opcodes.ASTORE, Opcodes.ALOAD, Opcodes.ARETURN) {

  constructor(realClazz: Class<*>): this(realClazz, emptyList())

  override fun withGenericTypes(genericTypes: List<JavaType>): JavaType {
    return LoadedObjectType(realClazz, genericTypes)
  }

}

class JavaPrimitiveType internal constructor(
  objectKlazz: KClass<*>,
  loadCode: Int,
  storeCode: Int,
  returnCode: Int,
  val addCode: Int,
  val subCode: Int,
  val mulCode: Int,
  val divCode: Int): LoadedJavaType(objectKlazz.javaPrimitiveType!!, emptyList(), storeCode, loadCode, returnCode) {

  val objectClass = objectKlazz.java
  override fun withGenericTypes(genericTypes: List<JavaType>): JavaPrimitiveType {
    throw SemanticException("Cannot have primitive type with generic types")
  }

  override fun raw(): JavaType {
    return this
  }
}