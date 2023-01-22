package com.tambapps.marcel.parser.type

import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.MarcelParsingException
import com.tambapps.marcel.parser.PrimitiveTypes
import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.ast.TypedNode
import com.tambapps.marcel.parser.exception.SemanticException
import org.objectweb.asm.Opcodes

// TODO add an imple for dynamic types e.g. class defined in a marcel script
open class JavaType internal constructor(
  val isDefined: Boolean,
  val realClassOrObject: Class<*>,
    val className: String,
    val internalName: String,
    val descriptor: String,
    val storeCode: Int,
    val loadCode: Int,
    val returnCode: Int,
    val genericTypes: List<JavaType>,
    val isInterface: Boolean): TypedNode {

  override val type: JavaType get() = this
  open val primitive = false

  constructor(clazz: Class<*>): this(true, clazz, clazz.name, AsmUtils.getInternalName(clazz), AsmUtils.getClassDescriptor(clazz),
  Opcodes.ASTORE, Opcodes.ALOAD, Opcodes.ARETURN, emptyList(), clazz.isInterface)

  init {
    if (genericTypes.any { it.primitive }) {
      throw MarcelParsingException("Cannot have a primitive type as generic type")
    }
  }

  // constructors for class defined in a script
  private constructor(clazz: String): this(false, Object.realClassOrObject, clazz,
    AsmUtils.getInternalName(clazz), AsmUtils.getObjectClassDescriptor(clazz), Opcodes.ASTORE, Opcodes.ALOAD, Opcodes.ARETURN, emptyList(), false)

  companion object {

    private val DEFINED_TYPES = mutableMapOf<String, JavaType>()

    fun defineClass(className: String): JavaType {
      try {
        Class.forName(className)
        throw SemanticException("Class $className is already defined")
      } catch (e: ClassNotFoundException) {
        // ignore
      }
      if (DEFINED_TYPES.containsKey(className)) throw SemanticException("Class $className is already defined")
      val type = JavaType(className)
      DEFINED_TYPES[className] = type
      return type
    }

    fun of(className: String): JavaType {
      if (DEFINED_TYPES.containsKey(className)) return DEFINED_TYPES.getValue(className)
      try {
        val clazz = Class.forName(className)
        val type = JavaType(clazz)
        DEFINED_TYPES[className] = type
        return type
      } catch (e: ClassNotFoundException) {
        throw SemanticException("Class $className was not found")
      }
    }

    fun clear() {
      DEFINED_TYPES.clear()
    }

    val Object = JavaType(Object::class.java)
    val String = JavaType(String::class.java)
    val Boolean = JavaType(Class.forName("java.lang.Boolean"))
    val Integer = JavaType(Class.forName("java.lang.Integer"))
    val Long = JavaType(Class.forName("java.lang.Long"))
    val Float = JavaType(Class.forName("java.lang.Float"))
    val Double = JavaType(Class.forName("java.lang.Double"))


    val void = JavaPrimitiveType(PrimitiveTypes.VOID, Opcodes.ALOAD, Opcodes.ASTORE, Opcodes.RETURN, 0,0,0,0)
    val int = JavaPrimitiveType(PrimitiveTypes.INT, Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN, Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL, Opcodes.IDIV)
    val long = JavaPrimitiveType(PrimitiveTypes.LONG, Opcodes.LLOAD, Opcodes.LSTORE, Opcodes.LRETURN, Opcodes.LADD, Opcodes.LSUB, Opcodes.LMUL, Opcodes.LDIV)
    val float = JavaPrimitiveType(PrimitiveTypes.FLOAT, Opcodes.FLOAD, Opcodes.FSTORE, Opcodes.FRETURN, Opcodes.FADD, Opcodes.FSUB, Opcodes.FMUL, Opcodes.FDIV)
    val double = JavaPrimitiveType(PrimitiveTypes.DOUBLE, Opcodes.DLOAD, Opcodes.DSTORE, Opcodes.DRETURN, Opcodes.DADD, Opcodes.DSUB, Opcodes.DMUL, Opcodes.DDIV)
    // apparently we use int instructions to store booleans
    val boolean = JavaPrimitiveType(PrimitiveTypes.BOOL, Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN,  0,0,0,0)
    // Marcel doesn't support char, but we could still find char values from plain Java code
    val char = JavaPrimitiveType(PrimitiveTypes.BOOL, Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN, Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL, Opcodes.IDIV)

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

  fun isAssignableFrom(javaType: JavaType): Boolean {
    if (this == javaType || this == Object && !javaType.primitive
      // to handle null values that can be cast to anything
      || !primitive && javaType == void) {
      return true
    }
    if (primitive || javaType.primitive) {
      return this == javaType
    }
    // TODO only handle (properly) classes already defined, not parsed class
    val thisClass = realClassOrObject
    val otherClass = javaType.realClassOrObject
    return thisClass.isAssignableFrom(otherClass)
  }

  open fun withGenericTypes(genericTypes: List<JavaType>): JavaType {
    return JavaType(isDefined, realClassOrObject, className, internalName, descriptor, storeCode, loadCode, returnCode, genericTypes, isInterface)
  }

  override fun toString(): String {
    if (genericTypes.isNotEmpty()) {
      return className + "<" + genericTypes.joinToString(separator = ", ") + ">"
    }
    return className
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is JavaType) return false

    if (className != other.className) return false
    if (genericTypes != other.genericTypes) return false

    return true
  }

  override fun hashCode(): Int {
    var result = className.hashCode()
    result = 31 * result + genericTypes.hashCode()
    return result
  }

}

class JavaPrimitiveType(
  realClassOrObject: Class<*>,
  className: String,
                        internalName: String,
                        descriptor: String,
                        loadCode: Int,
                        storeCode: Int,
                        returnCode: Int,
                        val addCode: Int,
                        val subCode: Int,
                        val mulCode: Int,
                        val divCode: Int): JavaType(true, realClassOrObject, className, internalName, descriptor, storeCode, loadCode, returnCode, emptyList(), false) {
  override val primitive = true
  internal constructor(clazz: Class<*>,
              loadCode: Int,
              storeCode: Int,
              retCode: Int,
              addCode: Int,
              subCode: Int,
              mulCode: Int,
              divCode: Int): this(clazz, clazz.name, AsmUtils.getInternalName(clazz),
      AsmUtils.getClassDescriptor(clazz), loadCode, storeCode, retCode, addCode, subCode, mulCode, divCode)

  override fun withGenericTypes(genericTypes: List<JavaType>): JavaPrimitiveType {
    throw SemanticException("Cannot have primitive type with generic types")
  }
}