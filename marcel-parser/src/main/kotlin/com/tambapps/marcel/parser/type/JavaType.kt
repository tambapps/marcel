package com.tambapps.marcel.parser.type

import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.MarcelParsingException
import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.ast.AstTypedObject
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.scope.Scope
import it.unimi.dsi.fastutil.booleans.BooleanArrayList
import it.unimi.dsi.fastutil.booleans.BooleanList
import it.unimi.dsi.fastutil.booleans.BooleanSet
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap
import it.unimi.dsi.fastutil.floats.Float2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import marcel.lang.lambda.Lambda
import marcel.lang.primitives.collections.lists.DoubleArrayList
import marcel.lang.primitives.collections.lists.DoubleList
import marcel.lang.primitives.collections.lists.FloatArrayList
import marcel.lang.primitives.collections.lists.FloatList
import marcel.lang.primitives.collections.lists.IntArrayList
import marcel.lang.primitives.collections.lists.IntList
import marcel.lang.primitives.collections.lists.LongArrayList
import marcel.lang.primitives.collections.lists.LongList
import marcel.lang.primitives.collections.sets.DoubleSet
import marcel.lang.primitives.collections.sets.FloatSet
import marcel.lang.primitives.collections.sets.IntSet
import marcel.lang.primitives.collections.sets.LongSet
import org.objectweb.asm.Opcodes
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass

interface JavaType: AstTypedObject {

  // whether the class is in the classpath and therefore can be accessed with Class.forName(className)
  val isLoaded: Boolean
  val realClazz: Class<*>
  val className: String
  val superType: JavaType?
  val internalName
    get() = AsmUtils.getInternalName(className)
  val descriptor: String

  val signature: String get() {
    if (primitive) return descriptor
    val builder = StringBuilder("L$internalName")
    if (genericTypes.isNotEmpty()) {
      genericTypes.joinTo(buffer = builder, separator = "", prefix = "<", postfix = ">", transform = { it.descriptor })
    }
    builder.append(";")
    directlyImplementedInterfaces.joinTo(buffer = builder, separator = "", transform = { it.signature })
    return builder.toString()
  }
  val hasGenericTypes: Boolean get() = genericTypes.isNotEmpty()
  val innerName: String? get() {
    val i = className.lastIndexOf('$')
    return if (i < 0) null else className.substring(i + 1)
  }
  val simpleName: String get() {
    val i = className.lastIndexOf('.')
    return if (i < 0) className else className.substring(i + 1)
  }

  val storeCode: Int
  val loadCode: Int
  val returnCode: Int
  val genericTypes: List<JavaType>
  val genericParameterNames: List<String>
  val isInterface: Boolean
  val isLambda get() = JavaType.lambda.isAssignableFrom(this)
  val primitive: Boolean
  open val isArray get() = isLoaded && realClazz.isArray
  override val type: JavaType get() = this
  val realClazzOrObject: Class<*>
  val directlyImplementedInterfaces: Collection<JavaType>
  val allImplementedInterfaces: Collection<JavaType>
  val asPrimitiveType: JavaPrimitiveType
    get() = throw RuntimeException("Compiler error: Illegal JavaType cast")
  val asArrayType: JavaArrayType
    get() = throw RuntimeException("Compiler error: Illegal JavaType cast")

  fun withGenericTypes(genericTypes: List<JavaType>): JavaType
  // return this type without generic types
  fun raw(): JavaType {
    return withGenericTypes(emptyList())
  }

  fun isAssignableFrom(other: JavaType): Boolean {
    if (this == other || this == Object && !other.primitive
      // to handle null values that can be cast to anything
      || !primitive && other == void) {
      return true
    }
    /*
    ignoring generic types for now
    if (genericTypes.size != other.genericTypes.size) return false
    for (i in genericTypes.indices) {
      if (!genericTypes[i].isAssignableFrom(other.genericTypes[i])) return false
    }
     */
    if (other.primitive && (
          this in listOf(of(java.lang.Object::class.java), of(Number::class.java), of(other.asPrimitiveType.objectClass))
        )) {
      return true
    }
    if (isLoaded && other.isLoaded) {
      return realClazz.isAssignableFrom(other.realClazz)
    } else {
      var otherSuperType = other.superType
      while (otherSuperType != null) {
        if (otherSuperType == this) return true
        otherSuperType = otherSuperType.superType
      }
      return false
    }
  }

  companion object {

    fun commonType(list: List<AstTypedObject>): JavaType {
      if (list.isEmpty()) return void
      return list.reduce { acc, javaType -> commonType(acc, javaType) }.type
    }

    fun commonType(aa: AstTypedObject, bb: AstTypedObject): JavaType {
      val a = aa.type
      val b = bb.type


      if (a == b) return a
      if (a.isAssignableFrom(b)) return a
      if (b.isAssignableFrom(a)) return b

      if (a.primitive && b.primitive) {
        if (a == int && b in listOf(long, double, float)) return b
        if (b == int && a in listOf(long, double, float)) return a
        if (a == float && b == double) return b
        if (b == float && a == double) return a
      } else {
        var aType = a
        var bType = b
        while (aType.superType != null && bType.superType != null) {
          aType = aType.superType!!
          bType = bType.superType!!
          if (aType.isAssignableFrom(bType)) return a
          if (bType.isAssignableFrom(aType)) return b
        }
      }
      return JavaType.Object
    }

    fun of(clazz: Class<*>): JavaType {
      return of(clazz, emptyList())
    }
    fun of(clazz: Class<*>, genericTypes: List<JavaType>): JavaType {
      return if (clazz.isPrimitive) PRIMITIVES.find { it.className == clazz.name } ?: throw RuntimeException("Primitive type $clazz is not being handled")
      else LoadedObjectType(clazz, genericTypes)
    }

    fun mapType(keysType: JavaType, valuesType: JavaType): JavaType {
      return if (keysType.primitive) of("map", listOf(keysType, valuesType))
      else of(java.util.Map::class.java, listOf(keysType, valuesType))
    }

    fun arrayType(elementsType: JavaType): JavaArrayType {
      if (!elementsType.primitive) {
        return objectArray
      }
      return when (elementsType) {
        int -> intArray
        long -> longArray
        float -> floatArray
        double -> doubleArray
        boolean -> booleanArray
        else -> throw MarcelParsingException("Doesn't handle primitive $elementsType arrays")
      }
    }
    fun of(className: String, genericTypes: List<JavaType>): JavaType {
      val optPrimitiveType = PRIMITIVES.find { it.className == className }
      if (optPrimitiveType != null) return optPrimitiveType
      val optArrayType = ARRAYS.find { it.className == className }
      if (optArrayType != null) return optArrayType

     if (genericTypes.size == 1 || className == "map" && genericTypes.size == 2) {
        val type = PRIMITIVE_COLLECTION_TYPE_MAP[className]?.get(genericTypes.first())
        if (type != null) return type
      }
      try {
        return of(Class.forName(className)).withGenericTypes(genericTypes)
      } catch (e: ClassNotFoundException) {
        throw SemanticException("Class $className was not found")
      }
    }

    fun lazy(scope: Scope, className: String, genericTypes: List<JavaType>): JavaType {
      return LazyJavaType(scope, className, genericTypes)
    }

    val Object = LoadedObjectType(Object::class.java)
    val String = LoadedObjectType(String::class.java)
    val Boolean = LoadedObjectType(Class.forName("java.lang.Boolean"))
    val Integer = LoadedObjectType(Class.forName("java.lang.Integer"))
    val Long = LoadedObjectType(Class.forName("java.lang.Long"))
    val Float = LoadedObjectType(Class.forName("java.lang.Float"))
    val Double = LoadedObjectType(Class.forName("java.lang.Double"))
    val Character = LoadedObjectType(Class.forName("java.lang.Character"))

    val void = JavaPrimitiveType(java.lang.Void::class, Opcodes.ALOAD, Opcodes.ASTORE, Opcodes.RETURN, 0,0,0,0)
    val int = JavaPrimitiveType(java.lang.Integer::class, Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN, Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL, Opcodes.IDIV)
    val long = JavaPrimitiveType(java.lang.Long::class, Opcodes.LLOAD, Opcodes.LSTORE, Opcodes.LRETURN, Opcodes.LADD, Opcodes.LSUB, Opcodes.LMUL, Opcodes.LDIV)
    val float = JavaPrimitiveType(java.lang.Float::class, Opcodes.FLOAD, Opcodes.FSTORE, Opcodes.FRETURN, Opcodes.FADD, Opcodes.FSUB, Opcodes.FMUL, Opcodes.FDIV)
    val double = JavaPrimitiveType(java.lang.Double::class, Opcodes.DLOAD, Opcodes.DSTORE, Opcodes.DRETURN, Opcodes.DADD, Opcodes.DSUB, Opcodes.DMUL, Opcodes.DDIV)
    // apparently we use int instructions to store booleans
    val boolean = JavaPrimitiveType(java.lang.Boolean::class, Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN,  0,0,0,0)
    // Marcel doesn't support char, but we could still find char values from plain Java code
    val char = JavaPrimitiveType(java.lang.Character::class, Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN, Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL, Opcodes.IDIV)
    // byte and short aren't supported in Marcel. The opcodes weren't verified
    val byte = JavaPrimitiveType(java.lang.Byte::class, Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN, Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL, Opcodes.IDIV)
    val short = JavaPrimitiveType(java.lang.Short::class, Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN, Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL, Opcodes.IDIV)

    val PRIMITIVES = listOf(void, int, long, float, double, boolean, char, byte, short)

    val intArray = JavaArrayType(IntArray::class.java, int, Opcodes.IASTORE, Opcodes.IALOAD, Opcodes.T_INT)
    val longArray = JavaArrayType(LongArray::class.java, long, Opcodes.LASTORE, Opcodes.LALOAD, Opcodes.T_LONG)
    val floatArray = JavaArrayType(FloatArray::class.java, float, Opcodes.FASTORE, Opcodes.FALOAD, Opcodes.T_FLOAT)
    val doubleArray = JavaArrayType(DoubleArray::class.java, double, Opcodes.DASTORE, Opcodes.DALOAD, Opcodes.T_DOUBLE)
    val booleanArray = JavaArrayType(BooleanArray::class.java, boolean, Opcodes.BASTORE, Opcodes.BALOAD, Opcodes.T_BOOLEAN)
    val objectArray = JavaArrayType(Array<Any>::class.java, Object, Opcodes.AASTORE, Opcodes.AALOAD, 0)
    val ARRAYS = listOf(intArray, longArray, floatArray, doubleArray, booleanArray, objectArray)

    val lambda = of(Lambda::class.java)

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

    // lists
    val intList = of(IntList::class.java)
    val intListImpl = of(IntArrayList::class.java)
    val longList = of(LongList::class.java)
    val longListImpl = of(LongArrayList::class.java)
    val floatList = of(FloatList::class.java)
    val floatListImpl = of(FloatArrayList::class.java)
    val doubleList = of(DoubleList::class.java)
    val doubleListImpl = of(DoubleArrayList::class.java)
    val booleanList = of(BooleanList::class.java)
    val booleanListImpl = of(BooleanArrayList::class.java)

    // lists
    val intSet = of(IntSet::class.java)
    val longSet = of(LongSet::class.java)
    val floatSet = of(FloatSet::class.java)
    val doubleSet = of(DoubleSet::class.java)

    // maps with primitive key
    val int2ObjectMap = of(Int2ObjectMap::class.java)
    val long2ObjectMap = of(Long2ObjectMap::class.java)
    val float2ObjectMap = of(Float2ObjectMap::class.java)
    val double2ObjectMap = of(Double2ObjectMap::class.java)

    internal val PRIMITIVE_COLLECTION_TYPE_MAP = mapOf(
      Pair("list", mapOf(
        Pair(int, intList),
        Pair(long, longList),
        Pair(float, floatList),
        Pair(double, doubleList),
        Pair(boolean, booleanList),
      )),
      Pair("set", mapOf(
        Pair(int, intSet),
        Pair(long, longSet),
        Pair(float, floatSet),
        Pair(double, doubleSet),
      )),
      Pair("map", mapOf(
        Pair(int, int2ObjectMap),
        Pair(long, long2ObjectMap),
        Pair(float, float2ObjectMap),
        Pair(double, double2ObjectMap),
        )),
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
}

class NotLoadedJavaType internal constructor(
  override val className: String,
  override val genericTypes: List<JavaType>,
  override val genericParameterNames: List<String>,
  override val superType: JavaType?,
  override val isInterface: Boolean,
  override val directlyImplementedInterfaces: Collection<JavaType>): AbstractJavaType() {

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

  override val allImplementedInterfaces: Collection<JavaType>
    get() {
      val allInterfaces = directlyImplementedInterfaces.toMutableSet()
      if (superType != null) allInterfaces.addAll(superType.allImplementedInterfaces)
      return allInterfaces
    }

  override fun withGenericTypes(genericTypes: List<JavaType>): JavaType {
    throw UnsupportedOperationException("Doesn't support generics for marcel classes (for now)")
  }
}

abstract class LoadedJavaType internal constructor(final override val realClazz: Class<*>, final override val genericTypes: List<JavaType>,
                              override val storeCode: Int, override val loadCode: Int, override val returnCode: Int): AbstractJavaType() {
  override val isLoaded = true
  override val descriptor: String
    get() = AsmUtils.getClassDescriptor(realClazz)

  override val className: String = realClazz.name
  override val superType get() =  if (realClazz.superclass != null) JavaType.of(realClazz.superclass) else null

  override val genericParameterNames: List<String>
    get() = realClazz.typeParameters.map { it.name }

  override val isInterface = realClazz.isInterface
  private var _interfaces: Set<JavaType>? = null
  override val allImplementedInterfaces: Collection<JavaType>
    get() {
      if (_interfaces == null) {
        _interfaces = getAllImplementedInterfacesRecursively(realClazz).asSequence()
          .toSet()
      }
      return _interfaces!!
    }

  override val directlyImplementedInterfaces: Collection<JavaType>
    get() = realClazz.interfaces.map { toJavaType(realClazz, it) }

  private fun toJavaType(realClazz: Class<*>, interfaze: Class<*>): JavaType {
    val genericInterface = realClazz.genericInterfaces
        .mapNotNull { it as? ParameterizedType }
        .find { it.rawType.typeName == interfaze.typeName }
    var type = JavaType.of(interfaze)
    if (genericInterface != null) {
      val thisClassGenericTypes = realClazz.typeParameters
      val interfaceTypeParameters = genericInterface.actualTypeArguments
      val genericTypes = interfaceTypeParameters.map { intTypeParam ->
        val i = thisClassGenericTypes.indexOfFirst { it.name == intTypeParam.typeName }
        return@map if (i >= 0 && i < genericTypes.size) genericTypes[i] else JavaType.Object
      }
      type = type.withGenericTypes(genericTypes)
    }
    return type
  }

  private fun getAllImplementedInterfacesRecursively(c: Class<*>): Set<JavaType> {
    var clazz = c
    val res = mutableSetOf<JavaType>()
    do {
      // First, add all the interfaces implemented by this class
      val interfaces = clazz.interfaces.map {
        toJavaType(clazz, it)
      }
      res.addAll(interfaces)
      for (interfaze in interfaces) {
        res.addAll(getAllImplementedInterfacesRecursively(interfaze.realClazz))
      }
      // Add the super class
      val superClass = clazz.superclass ?: break
      // Interfaces does not have java,lang.Object as superclass, they have null, so break the cycle and return
      // Now inspect the superclass
      clazz = superClass
    } while (JavaType.Object.realClazz != clazz)
    return res
  }

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
    if (genericTypes == this.genericTypes) return this
    if (genericTypes.any { it.primitive }) throw SemanticException("Cannot have a primitive generic type")
    if (isLambda && genericTypes.size == realClazz.typeParameters.size - 1) {
      return LoadedObjectType(realClazz, genericTypes + JavaType.Object)
    }

    if (genericTypes.size != realClazz.typeParameters.size
      // for lambda, we can omit return type. It will be cast
      && !isLambda && genericTypes.size != realClazz.typeParameters.size - 1) throw SemanticException("Typed $realClazz expects ${realClazz.typeParameters.size} parameters")
    return LoadedObjectType(realClazz, genericTypes)
  }

}

class JavaArrayType internal constructor(
  realClazz: Class<*>,
  val elementsType: JavaType,
  val arrayStoreCode: Int,
  val arrayLoadCode: Int,
  val typeCode: Int
): LoadedJavaType(realClazz, emptyList(), Opcodes.ASTORE, Opcodes.ALOAD, Opcodes.ARETURN) {
  override val isArray get() = true
  override val asArrayType: JavaArrayType
    get() = this

  override fun withGenericTypes(genericTypes: List<JavaType>): JavaType {
    throw SemanticException("Cannot have array type with generic types")
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
    if (genericTypes.isNotEmpty()) throw SemanticException("Cannot have primitive type with generic types")
    return this
  }

  override val asPrimitiveType: JavaPrimitiveType
    get() = this
  override fun raw(): JavaType {
    return this
  }
}

class LazyJavaType internal constructor(private val scope: Scope,
                                        private val actualTypeName: String, private val _genericTypes: List<JavaType>): AbstractJavaType() {

  private var _actualType: JavaType? = null
  private val actualType: JavaType
    get() {
      if (_actualType == null) {
        _actualType = scope.resolveType(actualTypeName, _genericTypes)
      }
      return _actualType!!
    }

  override val isLoaded: Boolean
    get() = actualType.isLoaded
  override val realClazz: Class<*>
    get() = actualType.realClazz
  override val className: String
    get() = actualType.className
  override val superType: JavaType?
    get() = actualType.superType
  override val descriptor: String
    get() = actualType.descriptor
  override val storeCode: Int
    get() = actualType.storeCode
  override val loadCode: Int
    get() = actualType.loadCode
  override val returnCode: Int
    get() = actualType.returnCode
  override val genericTypes: List<JavaType>
    get() = actualType.genericTypes
  override val genericParameterNames: List<String>
    get() = actualType.genericParameterNames
  override val isInterface: Boolean
    get() = actualType.isInterface
  override val primitive: Boolean
    get() = actualType.primitive
  override val realClazzOrObject: Class<*>
    get() = actualType.realClazzOrObject

  override val directlyImplementedInterfaces: Collection<JavaType>
    get() = actualType.directlyImplementedInterfaces
  override val allImplementedInterfaces: Collection<JavaType>
    get() = actualType.allImplementedInterfaces

  override val asArrayType: JavaArrayType
    get() = actualType.asArrayType
  override val asPrimitiveType: JavaPrimitiveType
    get() = actualType.asPrimitiveType
  override fun withGenericTypes(genericTypes: List<JavaType>): JavaType {
    return actualType.withGenericTypes(genericTypes)
  }
}