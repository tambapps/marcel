package com.tambapps.marcel.semantic.type

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.expression.literal.BoolConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.ByteConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.CharConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.DoubleConstantNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.literal.FloatConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.IntConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.LongConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.NullValueNode
import com.tambapps.marcel.semantic.ast.expression.literal.ShortConstantNode
import marcel.lang.lambda.Lambda
import marcel.lang.primitives.collections.CharCollection
import marcel.lang.primitives.collections.DoubleCollection
import marcel.lang.primitives.collections.FloatCollection
import marcel.lang.primitives.collections.IntCollection
import marcel.lang.primitives.collections.LongCollection
import marcel.lang.primitives.collections.lists.CharArrayList
import marcel.lang.primitives.collections.lists.CharList
import marcel.lang.primitives.collections.lists.DoubleArrayList
import marcel.lang.primitives.collections.lists.DoubleList
import marcel.lang.primitives.collections.lists.FloatArrayList
import marcel.lang.primitives.collections.lists.FloatList
import marcel.lang.primitives.collections.lists.IntArrayList
import marcel.lang.primitives.collections.lists.IntList
import marcel.lang.primitives.collections.lists.LongArrayList
import marcel.lang.primitives.collections.lists.LongList
import marcel.lang.primitives.collections.sets.CharSet
import marcel.lang.primitives.collections.sets.DoubleSet
import marcel.lang.primitives.collections.sets.FloatSet
import marcel.lang.primitives.collections.sets.IntSet
import marcel.lang.primitives.collections.sets.LongSet
import java.util.concurrent.Future

/**
 * Represents a Java class that may or may not be loaded on the classpath
 *
 * @property isLoaded whether the class is loaded on the classpath
 * @property realClazz get the [Class] representing this type. Only available for loaded types
 * @property isEnum whether this type represents an enum class or not
 * @property className the class full name
 * @property packageName the class package name (may be null)
 * @property superType the class super type
 * @property isAnnotation whether this type represents an annotation class or not
 * @property isFinal whether the class is declared as final
 * @property visibility the visibility of the class
 * @property isScript whether the class was declared from a marcel script source file
 * @property objectType returns the object type representing this class. E.g. for the primitive [int] class it will return [Integer], and for object types it will return itself
 * @property isTopLevel whether the class is top level, has no outer type above it
 * @property hasGenericTypes whether this type has generic types specified
 * @property innerName the inner name of this class (for non-top-level classes) or null
 * @property outerTypeName the outer type full name of the class (for non-top-level classes) or null
 * @property simpleName the simple name of this type
 * @property takes2Slots whether this type takes 2 Java ARM slots (for long and doubles)
 * @property nbSlots the number of Java ARM slots this type takes
 * @property type returns itself
 * @property genericTypes the generic types specified
 * @property isLambda whether this type represents a lambda
 * @property primitive whether this type represents a primitive class
 * @property isPrimitiveObjectType whether this type represents the [objectType] of a primitive type
 * @property isPrimitiveOrObjectPrimitive whether this type represents a primitive type or an [objectType] of a primitive type
 * @property arrayType returns the array type whose elements would be of this type
 * @property isArray whether this class represents an array type
 * @property directlyImplementedInterfaces the collection of directly implemented interfaces of this type
 * @property allImplementedInterfaces the collection of all the implemented interfaces of this type
 * @property asPrimitiveType returns this type as a [JavaPrimitiveType] or fail if it not
 * @property asArrayType returns this type as a [JavaArrayType] or fail if it not
 * @property asAnnotationType returns this type as a [JavaAnnotationType] or fail if it not
 */
interface JavaType: JavaTyped {

  // whether the class is in the classpath and therefore can be accessed with Class.forName(className)
  val isLoaded: Boolean
  val realClazz: Class<*>
  val isEnum: Boolean
  val className: String
  val packageName: String?
  val superType: JavaType?
  val isAnnotation: Boolean
  val isFinal: Boolean
  val visibility: Visibility
  val isScript: Boolean
  val objectType: JavaType get() = this
  val isTopLevel get() = !className.contains("$")

  val hasGenericTypes: Boolean get() = genericTypes.isNotEmpty()
  val innerName: String? get() {
    val i = className.lastIndexOf('$')
    return if (i < 0) null else className.substring(i + 1)
  }

  /**
   * Retrieves the annotation of the given type or null
   *
   * @param javaAnnotationType the type of the annotation
   * @return the annotation of the given type or null
   */
  fun getAnnotation(javaAnnotationType: JavaAnnotationType): JavaAnnotation?

  val outerTypeName: String? get() {
    val i = className.lastIndexOf('$')
    return if (i < 0) null else className.substring(0, i)
  }

  /**
   * Returns whether this class is an outer class (no matter the level) of the provided one
   *
   * @param javaType
   * @return whether this class is an outer class (no matter the level) of the provided one
   */
  fun isOuterTypeOf(javaType: JavaType) = javaType.className.contains("$className$")

  val simpleName: String get() {
    val i = className.lastIndexOf('.')
    return if (i < 0) className else className.substring(i + 1)
  }

  val takes2Slots get() = this == long || this == double
  val nbSlots get() = if (takes2Slots) 2 else 1

  override val type get() = this
  val genericTypes: List<JavaType>
  val isInterface: Boolean
  val isLambda get() = JavaType.lambda.isAssignableFrom(this)
  val primitive: Boolean
  val isPrimitiveObjectType get() = PRIMITIVES.any { it.objectType == this }
  val isPrimitiveOrObjectPrimitive get() = primitive || isPrimitiveObjectType

  val arrayType: JavaArrayType

  /**
   * Returns the array type top of n [dimensions] of this type
   *
   * @param dimensions the number of dimensions of this type
   * @return the array type top of n [dimensions] of this type
   */
  fun array(dimensions: Int): JavaType {
    var type = this
    for (i in 0 until dimensions) {
      type = type.arrayType
    }
    return type
  }

  /**
   * Returns whether this type is accessible from the provided type
   *
   * @param javaType the other type
   * @return whether this type is accessible from the provided type
   */
  fun isAccessibleFrom(javaType: JavaType) = visibility.canAccess(javaType, this)

  val isArray: Boolean

  val directlyImplementedInterfaces: Collection<JavaType>
  val allImplementedInterfaces: Collection<JavaType>
  val asPrimitiveType: JavaPrimitiveType
    get() = throw RuntimeException("Illegal JavaType cast")
  val asArrayType: JavaArrayType
    get() = throw RuntimeException("Illegal JavaType cast")
  val asAnnotationType: JavaAnnotationType
    get() = throw RuntimeException("Illegal JavaType cast")

  /**
   * Get the default value expression of this type.
   * 0 for number primitives, false for boolean, and null for object types
   *
   * @param token the token representing the expression
   * @return the default value expresion of this type
   */
  fun getDefaultValueExpression(token: LexToken): ExpressionNode

  /**
   * Returns this type with the provided generic types specified
   *
   * @param genericTypes the generic types to specify
   * @return this type with the provided generic types specified
   */
  fun withGenericTypes(vararg genericTypes: JavaType): JavaType {
    return withGenericTypes(genericTypes.toList())
  }

  /**
   * Returns this type with the provided generic types specified
   *
   * @param genericTypes the generic types to specify
   * @return this type with the provided generic types specified
   */
  fun withGenericTypes(genericTypes: List<JavaType>): JavaType

  /**
   * Returns this type without generic types
   *
   * @return this type without generic types
   */
  fun raw(): JavaType {
    return if (genericTypes.isEmpty()) this else withGenericTypes(emptyList())
  }

  /**
   * Returns whether the [other] type is this or a parent type of this
   *
   * @param other the other type
   * @return whether the [other] type is this or a parent type of this
   */
  fun isSelfOrSuper(other: JavaType) = other == this || other.isExtendedOrImplementedBy(this)

  /**
   * Returns whether this type is extended/implemented by the [other] type
   *
   * @param other the other type
   * @return whether this type is extended/implemented by the [other] type
   */
  fun isExtendedOrImplementedBy(other: JavaType): Boolean {
    if (isLoaded && other.isLoaded) {
      return realClazz.isAssignableFrom(other.realClazz)
    } else if (this.isInterface) {
      return other.implements(this, false)
    } else {
      var otherSuperType = other.superType
      while (otherSuperType != null) {
        if (otherSuperType == this) return true
        otherSuperType = otherSuperType.superType
      }
      return false
    }
  }

  /**
   * Returns whether this type is assignable from the [other].
   * In other words whether if we had a variable of this type, we could assign safely
   * a value of [other] type, without any cast checks
   *
   * @param other the other type
   * @return whether this type is assignable from the [other]
   */
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

    // We don't smartly cast primitive types here because it would induce the compiler in error when checking types, especially when casting

    return isExtendedOrImplementedBy(other)
  }

  /**
   * Whether this type implements the provided [javaType]
   *
   * @param javaType the other type
   * @param compareGenerics whether to also compare generic types or not
   * @return whether this type implements the provided [javaType]
   */
  fun implements(javaType: JavaType, compareGenerics: Boolean = false): Boolean {
    return (
        if (compareGenerics) this == javaType
        else this.className == javaType.className
        ) || allImplementedInterfaces.any {
      if (compareGenerics) javaType == it
      else javaType.className == it.className
    }
  }

  /**
   * Util static methods
   */
  companion object {

    /**
     *  Returns the common type between all the provided typed object
     *
     * @param list the list of types
     * @return the common type between all the provided typed object
     */
    fun commonType(list: List<JavaTyped>): JavaType {
      if (list.isEmpty()) return void
      return list.reduce { acc, javaType -> commonType(acc, javaType) }.type
    }


    /**
     * Returns the common type between the 2 types
     *
     * @param a the first type
     * @param b the second type
     * @return the common type between the 2 types
     */
    fun commonType(a: JavaTyped, b: JavaTyped) = commonType(a.type, b.type)

    /**
     * Returns the common type between the 2 types. Not that the common type may not be assignable from [a] or [b]
     * but both [a] and [b] should always be Marcel-casted into the common type.
     *
     * @param a the first type
     * @param b the second type
     * @return the common type between the 2 types
     */
    fun commonType(a: JavaType, b: JavaType): JavaType {
      if (a == b) return if (a === Anything) Object else a
      if (a === Anything) return b.objectType
      if (b === Anything) return a.objectType
      if (a.isAssignableFrom(b)) return a
      if (b.isAssignableFrom(a)) return b

      if (a.primitive && b.primitive) {
        if (a == double || b == double
          || a == long && b == float || a == float && b == long) return double
        if (a == float || b == float) return float
        if (a == long || b == long) return long
        if (a == int && b == char || a == char && b == int) return int
        if (a == short || b == short) return short
        if (a == byte || b == byte) return byte
      } else {
        var aType = a
        var bType = b
        while (aType.superType != null && bType.superType != null) {
          aType = aType.superType!!
          bType = bType.superType!!
          if (aType.isAssignableFrom(bType)) return aType
          if (bType.isAssignableFrom(aType)) return bType
        }
      }
      return JavaType.Object
    }

    // caching to avoid creating new instances each time we want a type
    private val TYPES_CACHE = mutableMapOf<Class<*>, JavaType>()

    /**
     * Returns the [JavaType] representing the provided [Class]
     *
     * @param clazz the java class
     * @return the [JavaType] representing this [Class]
     */
    fun of(clazz: Class<*>): JavaType {
      return of(clazz, emptyList())
    }

    /**
     * Returns the [JavaType] representing the provided [Class] with the provided generic types to specify
     *
     * @param clazz the java class
     * @param genericTypes the generic types to specify
     * @return the [JavaType] representing this [Class]
     */
    fun of(clazz: Class<*>, genericTypes: List<JavaType>): JavaType {
      val t = TYPES_CACHE.computeIfAbsent(clazz, this::getOrCreate)
      return if (genericTypes.isEmpty()) t else t.withGenericTypes(genericTypes)
    }

    private fun getOrCreate(clazz: Class<*>): JavaType {
      return if (clazz.isPrimitive) PRIMITIVES.find { it.className == clazz.name } ?: throw RuntimeException("Primitive type $clazz is not being handled")
      else if (clazz.isArray)
        ARRAYS.find { it.realClazz == clazz } ?: LoadedJavaArrayType(clazz)
      else if (clazz.isAnnotation) LoadedJavaAnnotationType(clazz)
      else LoadedObjectType(clazz, emptyList())
    }

    val Anything: JavaType = AnythingJavaType
    val Object = LoadedObjectType(Object::class.java)
    val Clazz = LoadedObjectType(Class::class.java)
    val String = LoadedObjectType(String::class.java)
    val DynamicObject = LoadedObjectType(marcel.lang.DynamicObject::class.java)
    val Boolean = LoadedObjectType(Class.forName("java.lang.Boolean"))
    val Integer = LoadedObjectType(Class.forName("java.lang.Integer"))
    val Long = LoadedObjectType(Class.forName("java.lang.Long"))
    val Float = LoadedObjectType(Class.forName("java.lang.Float"))
    val Double = LoadedObjectType(Class.forName("java.lang.Double"))
    val Character = LoadedObjectType(Class.forName("java.lang.Character"))
    val Byte = LoadedObjectType(Class.forName("java.lang.Byte"))
    val Short = LoadedObjectType(Class.forName("java.lang.Short"))
    val Void = LoadedObjectType(Class.forName("java.lang.Void"))
    val Map = of(Map::class.java)
    val Set = of(Set::class.java)

    val Future = LoadedObjectType(Future::class.java)

    val void = JavaPrimitiveType(java.lang.Void::class, false) { NullValueNode(it) }
    val int = JavaPrimitiveType(java.lang.Integer::class, true) { IntConstantNode(it, value = 0) }
    val long = JavaPrimitiveType(java.lang.Long::class, true) { LongConstantNode(it, value = 0L) }
    val float = JavaPrimitiveType(java.lang.Float::class, true) { FloatConstantNode(it, value = 0f) }
    val double = JavaPrimitiveType(java.lang.Double::class, true) { DoubleConstantNode(it, value = 0.0) }
    // apparently we use int instructions to store booleans
    val boolean = JavaPrimitiveType(java.lang.Boolean::class, false) { BoolConstantNode(it, value = false) }

    val char = JavaPrimitiveType(java.lang.Character::class, true) { CharConstantNode(it, value = 0.toChar()) }
    // byte and short aren't supported in Marcel. The opcodes weren't verified
    val byte = JavaPrimitiveType(java.lang.Byte::class, true) { ByteConstantNode(it, value = 0) }
    val short = JavaPrimitiveType(java.lang.Short::class, true) { ShortConstantNode(it, value = 0) }

    val PRIMITIVES = listOf(void, int, long, float, double, boolean, char, byte, short)

    val intArray = LoadedJavaArrayType(IntArray::class.java, int)
    val longArray = LoadedJavaArrayType(LongArray::class.java, long)
    val floatArray = LoadedJavaArrayType(FloatArray::class.java, float)
    val doubleArray = LoadedJavaArrayType(DoubleArray::class.java, double)
    val booleanArray = LoadedJavaArrayType(BooleanArray::class.java, boolean)
    val shortArray = LoadedJavaArrayType(ShortArray::class.java, short)
    val byteArray = LoadedJavaArrayType(ByteArray::class.java, byte)
    val charArray = LoadedJavaArrayType(CharArray::class.java, char)
    val objectArray = LoadedJavaArrayType(Array<Any>::class.java, Object)
    val ARRAYS = listOf(intArray, longArray, floatArray, doubleArray, booleanArray, shortArray, byteArray, charArray, objectArray)

    val lambda = of(Lambda::class.java)

    val IntRange = LoadedObjectType(marcel.lang.IntRange::class.java)
    val LongRange = LoadedObjectType(marcel.lang.LongRange::class.java)

    // collections
    val intCollection = of(IntCollection::class.java)
    val longCollection = of(LongCollection::class.java)
    val floatCollection = of(FloatCollection::class.java)
    val doubleCollection = of(DoubleCollection::class.java)
    val charCollection = of(CharCollection::class.java)

    // lists
    val intList = of(IntList::class.java)
    val intListImpl = of(IntArrayList::class.java)
    val longList = of(LongList::class.java)
    val longListImpl = of(LongArrayList::class.java)
    val floatList = of(FloatList::class.java)
    val floatListImpl = of(FloatArrayList::class.java)
    val doubleList = of(DoubleList::class.java)
    val doubleListImpl = of(DoubleArrayList::class.java)
    val charList = of(CharList::class.java)
    val charListImpl = of(CharArrayList::class.java)

    // lists
    val intSet = of(IntSet::class.java)
    val longSet = of(LongSet::class.java)
    val floatSet = of(FloatSet::class.java)
    val doubleSet = of(DoubleSet::class.java)
    val characterSet = of(CharSet::class.java)

    internal val PRIMITIVE_COLLECTION_TYPE_MAP = mapOf(
      Pair("list", mapOf(
        Pair(int, intList),
        Pair(long, longList),
        Pair(float, floatList),
        Pair(double, doubleList),
        Pair(char, charList),
      )),
      Pair("set", mapOf(
        Pair(int, intSet),
        Pair(long, longSet),
        Pair(float, floatSet),
        Pair(double, doubleSet),
        Pair(char, characterSet),
      )),
    )
  }

}
