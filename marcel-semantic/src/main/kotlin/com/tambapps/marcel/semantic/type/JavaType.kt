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
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
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

/**
 * Represents a Java class
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

  fun getAnnotation(javaAnnotationType: JavaAnnotationType): JavaAnnotation?

  val outerTypeName: String? get() {
    val i = className.lastIndexOf('$')
    return if (i < 0) null else className.substring(0, i)
  }

  // returns whether this class is an outer class (no matter the level) of the provided one
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

  val arrayType: JavaArrayType get() {
    if (primitive) {
      return when (this) {
        int -> intArray
        long -> longArray
        float -> floatArray
        double -> doubleArray
        boolean -> booleanArray
        char -> charArray
        else -> throw MarcelSemanticException(LexToken.DUMMY, "Doesn't handle primitive $this arrays")
      }
    }
    // this is the only way to get the array class of a class, pre java 12
    return if (this.isLoaded) LoadedJavaArrayType(java.lang.reflect.Array.newInstance(this.realClazz, 0).javaClass)
    else NotLoadedJavaArrayType(this)
  }

  fun array(dimensions: Int): JavaType {
    var type = this
    for (i in 0 until dimensions) {
      type = type.arrayType
    }
    return type
  }

  fun isAccessibleFrom(javaType: JavaType) = visibility.canAccess(javaType, this)

  val isArray: Boolean

  val realClazzOrObject: Class<*>
  val directlyImplementedInterfaces: Collection<JavaType>
  val allImplementedInterfaces: Collection<JavaType>
  val asPrimitiveType: JavaPrimitiveType
    get() = throw RuntimeException("Illegal JavaType cast")
  val asArrayType: JavaArrayType
    get() = throw RuntimeException("Illegal JavaType cast")
  val asAnnotationType: JavaAnnotationType
    get() = throw RuntimeException("Illegal JavaType cast")
  fun getDefaultValueExpression(token: LexToken): ExpressionNode

  fun withGenericTypes(vararg genericTypes: JavaType): JavaType {
    return withGenericTypes(genericTypes.toList())
  }

  fun withGenericTypes(genericTypes: List<JavaType>): JavaType
  // return this type without generic types
  fun raw(): JavaType {
    return if (genericTypes.isEmpty()) this else withGenericTypes(emptyList())
  }

  fun isSelfOrSuper(other: JavaType) = other == this || other.isExtendedOrImplementedBy(this)
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

  fun implements(javaType: JavaType, compareGenerics: Boolean = false): Boolean {
    return (
        if (compareGenerics) this == javaType
        else this.className == javaType.className
        ) || allImplementedInterfaces.any {
      if (compareGenerics) javaType == it
      else javaType.className == it.className
    }
  }

  companion object {

    fun commonType(list: List<JavaTyped>): JavaType {
      if (list.isEmpty()) return void
      return list.reduce { acc, javaType -> commonType(acc, javaType) }.type
    }

    fun commonType(a: JavaTyped, b: JavaTyped) = commonType(a.type, b.type)

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
      else if (clazz.isArray)
        ARRAYS.find { it.realClazz == clazz } ?: LoadedJavaArrayType(clazz)
      else if (clazz.isAnnotation) LoadedJavaAnnotationType(clazz)
      else LoadedObjectType(clazz, genericTypes)
    }

    fun arrayTypeFrom(type: JavaType): JavaArrayType? {
      return if (type.isArray) return type.asArrayType
      else if (intList.isAssignableFrom(type) || intSet.isAssignableFrom(type)) return intArray
      else if (longList.isAssignableFrom(type) || longSet.isAssignableFrom(type)) return longArray
      else if (charList.isAssignableFrom(type) || characterSet.isAssignableFrom(type)) return charArray
      else if (floatList.isAssignableFrom(type)) return floatArray
      else if (doubleList.isAssignableFrom(type)) return doubleArray
      else if (of(List::class.java).isAssignableFrom(type)) return objectArray
      else null
    }

    val Anything: JavaType = AnythingJavaType
    val Object = LoadedObjectType(Object::class.java)
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
