package com.tambapps.marcel.parser.type

import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.ast.AstTypedObject
import com.tambapps.marcel.parser.ast.expression.BooleanConstantNode
import com.tambapps.marcel.parser.ast.expression.ByteConstantNode
import com.tambapps.marcel.parser.ast.expression.CharConstantNode
import com.tambapps.marcel.parser.ast.expression.DoubleConstantNode
import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.ast.expression.FloatConstantNode
import com.tambapps.marcel.parser.ast.expression.IntConstantNode
import com.tambapps.marcel.parser.ast.expression.LongConstantNode
import com.tambapps.marcel.parser.ast.expression.NullValueNode
import com.tambapps.marcel.parser.ast.expression.ShortConstantNode
import com.tambapps.marcel.parser.ast.expression.VoidExpression
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import com.tambapps.marcel.parser.scope.Scope
import marcel.lang.DynamicObject
import marcel.lang.MarcelClassLoader
import marcel.lang.Script
import marcel.lang.lambda.Lambda
import marcel.lang.primitives.collections.lists.CharacterArrayList
import marcel.lang.primitives.collections.lists.CharacterList
import marcel.lang.primitives.collections.lists.DoubleArrayList
import marcel.lang.primitives.collections.lists.DoubleList
import marcel.lang.primitives.collections.lists.FloatArrayList
import marcel.lang.primitives.collections.lists.FloatList
import marcel.lang.primitives.collections.lists.IntArrayList
import marcel.lang.primitives.collections.lists.IntList
import marcel.lang.primitives.collections.lists.LongArrayList
import marcel.lang.primitives.collections.lists.LongList
import marcel.lang.primitives.collections.maps.Character2ObjectMap
import marcel.lang.primitives.collections.maps.Int2ObjectMap
import marcel.lang.primitives.collections.maps.Long2ObjectMap
import marcel.lang.primitives.collections.sets.CharacterSet
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
  val isScript get() = JavaType.of(Script::class.java).isAssignableFrom(this)
  val realClazz: Class<*>
  val className: String
  val packageName: String?
  val superType: JavaType?
  val isAnnotation: Boolean
  val internalName
    get() = AsmUtils.getInternalName(className)
  val descriptor: String
  val objectType: JavaType get() = this
  val isTopLevel get() = !className.contains("$")

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
  val isPrimitiveObjectType get() = PRIMITIVES.any { it.objectType == this }
  val isPrimitiveOrObjectPrimitive get() = primitive || isPrimitiveObjectType

  val arrayType: JavaArrayType get() = JavaType.arrayType(this)

  open val isArray get() = isLoaded && realClazz.isArray
  override val type: JavaType get() = this
  val realClazzOrObject: Class<*>
  val directlyImplementedInterfaces: Collection<JavaType>
  val allImplementedInterfaces: Collection<JavaType>
  val asPrimitiveType: JavaPrimitiveType
    get() = throw RuntimeException("Compiler error: Illegal JavaType cast")
  val asArrayType: JavaArrayType
    get() = throw RuntimeException("Compiler error: Illegal JavaType cast")
  val defaultValueExpression: ExpressionNode

  fun withGenericTypes(vararg genericTypes: JavaType): JavaType {
    return withGenericTypes(genericTypes.toList())
  }

  fun withGenericTypes(genericTypes: List<JavaType>): JavaType
  // return this type without generic types
  fun raw(): JavaType {
    return withGenericTypes(emptyList())
  }

  fun addImplementedInterface(javaType: JavaType) {
    throw MarcelSemanticException("Compiler error: Cannot add interface to type")
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

    fun newType(outerClassType: JavaType?, cName: String, superClass: JavaType, isInterface: Boolean, interfaces: List<JavaType>): JavaType {
      val className = if (outerClassType != null) "${outerClassType.className}\$$cName" else cName
      return NotLoadedJavaType(className, emptyList(), emptyList(),  superClass, isInterface, interfaces.toMutableList())
    }

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
        if (a == int && b == char || a == char && b == int) return int
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
      else if (clazz.isArray)
        ARRAYS.find { it.realClazz == clazz } ?: LoadedJavaArrayType(clazz)
      else LoadedObjectType(clazz, genericTypes)
    }

    fun mapType(keysType: JavaType, valuesType: JavaType): JavaType {
      return if (keysType.primitive) of("map", listOf(keysType, valuesType))
      else of(java.util.Map::class.java, listOf(keysType, valuesType))
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

    fun arrayType(elementsType: JavaType): JavaArrayType {
      if (elementsType.primitive) {
        return when (elementsType) {
          int -> intArray
          long -> longArray
          float -> floatArray
          double -> doubleArray
          boolean -> booleanArray
          char -> charArray
          else -> throw MarcelSemanticException("Doesn't handle primitive $elementsType arrays")
        }
      }

      // this is the only way to get the array class of a class, pre java 12
      return if (elementsType.isLoaded) LoadedJavaArrayType(java.lang.reflect.Array.newInstance(elementsType.realClazz, 0).javaClass)
      else NotLoadedJavaArrayType(elementsType)
    }
    fun of(className: String, genericTypes: List<JavaType>): JavaType {
      return of(null, className, genericTypes)
    }
    fun of(classLoader: MarcelClassLoader?, className: String, genericTypes: List<JavaType>): JavaType {
      val optPrimitiveType = PRIMITIVES.find { it.className == className }
      if (optPrimitiveType != null) return optPrimitiveType
      val optArrayType = ARRAYS.find { it.className == className }
      if (optArrayType != null) return optArrayType

     if (genericTypes.size == 1 || className == "map" && genericTypes.size == 2) {
        val type = PRIMITIVE_COLLECTION_TYPE_MAP[className]?.get(genericTypes.first())
        if (type != null) return type
      }
      try {
        val clazz = if (classLoader != null) classLoader.loadClass(className)
        else Class.forName(className)
        return of(clazz).withGenericTypes(genericTypes)
      } catch (e: ClassNotFoundException) {
        throw MarcelSemanticException("Class $className was not found")
      }
    }

    fun lazy(scope: Scope, className: String, genericTypes: List<JavaType>): JavaType {
      return LazyJavaType(scope, className, genericTypes)
    }

    val Object = LoadedObjectType(Object::class.java)
    val String = LoadedObjectType(String::class.java)
    val DynamicObject = LoadedObjectType(DynamicObject::class.java)
    val Boolean = LoadedObjectType(Class.forName("java.lang.Boolean"))
    val Integer = LoadedObjectType(Class.forName("java.lang.Integer"))
    val Long = LoadedObjectType(Class.forName("java.lang.Long"))
    val Float = LoadedObjectType(Class.forName("java.lang.Float"))
    val Double = LoadedObjectType(Class.forName("java.lang.Double"))
    val Character = LoadedObjectType(Class.forName("java.lang.Character"))

    val void = JavaPrimitiveType(java.lang.Void::class, Opcodes.ALOAD, Opcodes.ASTORE, Opcodes.RETURN, 0,0,0,0, VoidExpression())
    val int = JavaPrimitiveType(java.lang.Integer::class, Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN, Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL, Opcodes.IDIV, IntConstantNode(value = 0))
    val long = JavaPrimitiveType(java.lang.Long::class, Opcodes.LLOAD, Opcodes.LSTORE, Opcodes.LRETURN, Opcodes.LADD, Opcodes.LSUB, Opcodes.LMUL, Opcodes.LDIV, LongConstantNode(value = 0))
    val float = JavaPrimitiveType(java.lang.Float::class, Opcodes.FLOAD, Opcodes.FSTORE, Opcodes.FRETURN, Opcodes.FADD, Opcodes.FSUB, Opcodes.FMUL, Opcodes.FDIV, FloatConstantNode(value = 0f))
    val double = JavaPrimitiveType(java.lang.Double::class, Opcodes.DLOAD, Opcodes.DSTORE, Opcodes.DRETURN, Opcodes.DADD, Opcodes.DSUB, Opcodes.DMUL, Opcodes.DDIV, DoubleConstantNode(value = 0.0))
    // apparently we use int instructions to store booleans
    val boolean = JavaPrimitiveType(java.lang.Boolean::class, Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN,  0,0,0,0, BooleanConstantNode(value = false))

    val char = JavaPrimitiveType(java.lang.Character::class, Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN, Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL, Opcodes.IDIV, CharConstantNode(value = 0.toChar() + ""))
    // byte and short aren't supported in Marcel. The opcodes weren't verified
    val byte = JavaPrimitiveType(java.lang.Byte::class, Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN, Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL, Opcodes.IDIV, ByteConstantNode(value = 0))
    val short = JavaPrimitiveType(java.lang.Short::class, Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN, Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL, Opcodes.IDIV, ShortConstantNode(value = 0))

    val PRIMITIVES = listOf(void, int, long, float, double, boolean, char, byte, short)

    val intArray = LoadedJavaArrayType(IntArray::class.java, int, Opcodes.IASTORE, Opcodes.IALOAD, Opcodes.T_INT)
    val longArray = LoadedJavaArrayType(LongArray::class.java, long, Opcodes.LASTORE, Opcodes.LALOAD, Opcodes.T_LONG)
    val floatArray = LoadedJavaArrayType(FloatArray::class.java, float, Opcodes.FASTORE, Opcodes.FALOAD, Opcodes.T_FLOAT)
    val doubleArray = LoadedJavaArrayType(DoubleArray::class.java, double, Opcodes.DASTORE, Opcodes.DALOAD, Opcodes.T_DOUBLE)
    val booleanArray = LoadedJavaArrayType(BooleanArray::class.java, boolean, Opcodes.BASTORE, Opcodes.BALOAD, Opcodes.T_BOOLEAN)
    val shortArray = LoadedJavaArrayType(ShortArray::class.java, short, Opcodes.SASTORE, Opcodes.SALOAD, Opcodes.T_SHORT)
    val byteArray = LoadedJavaArrayType(ByteArray::class.java, byte, Opcodes.BASTORE, Opcodes.BALOAD, Opcodes.T_BYTE)
    val charArray = LoadedJavaArrayType(CharArray::class.java, char, Opcodes.CASTORE, Opcodes.CALOAD, Opcodes.T_CHAR)
    val objectArray = LoadedJavaArrayType(Array<Any>::class.java, Object, Opcodes.AASTORE, Opcodes.AALOAD, 0)
    val ARRAYS = listOf(intArray, longArray, floatArray, doubleArray, booleanArray, shortArray, byteArray, objectArray)

    val lambda = of(Lambda::class.java)

    val PRIMITIVE_CAST_INSTRUCTION_MAP = mapOf(
      Pair(Pair(int, long), Opcodes.I2L),
      Pair(Pair(int, float), Opcodes.I2F),
      Pair(Pair(int, double), Opcodes.I2D),
      Pair(Pair(int, boolean), Opcodes.I2B),
      Pair(Pair(int, char), Opcodes.I2C),
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
      Pair(TokenType.TYPE_CHAR, char),
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
    val charList = of(CharacterList::class.java)
    val charListImpl = of(CharacterArrayList::class.java)

    // lists
    val intSet = of(IntSet::class.java)
    val longSet = of(LongSet::class.java)
    val floatSet = of(FloatSet::class.java)
    val doubleSet = of(DoubleSet::class.java)
    val characterSet = of(CharacterSet::class.java)

    // maps with primitive key
    val int2ObjectMap = of(Int2ObjectMap::class.java)
    val long2ObjectMap = of(Long2ObjectMap::class.java)
    val char2ObjectMap = of(Character2ObjectMap::class.java)

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
      Pair("map", mapOf(
        Pair(int, int2ObjectMap),
        Pair(long, long2ObjectMap),
        Pair(char, char2ObjectMap),
        )),
    )
  }

}

abstract class AbstractJavaType: JavaType {

  override val defaultValueExpression: ExpressionNode = NullValueNode()
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

open class NotLoadedJavaType internal constructor(
  override val className: String,
  override val genericTypes: List<JavaType>,
  override val genericParameterNames: List<String>,
  override val superType: JavaType?,
  override val isInterface: Boolean,
  override val directlyImplementedInterfaces: MutableCollection<JavaType>): AbstractJavaType() {

  override val arrayType: JavaArrayType
    get() = this as NotLoadedJavaArrayType

  override val packageName: String?
    get() = if (className.contains('.')) className.substring(0, className.lastIndexOf(".")) else null

  override val isLoaded = false
  override val realClazz: Class<*>
    get() = throw RuntimeException("Class $className is not loaded")
  override val primitive = false
  override val realClazzOrObject = java.lang.Object::class.java
  override val storeCode = Opcodes.ASTORE
  override val loadCode = Opcodes.ALOAD
  override val returnCode = Opcodes.ARETURN
  override val descriptor: String
    get() {
      val descriptor = AsmUtils.getObjectClassDescriptor(className)
      return if (isAnnotation) "@$descriptor" else descriptor
    }
  override val isAnnotation = false

  override val allImplementedInterfaces: Collection<JavaType>
    get() {
      val allInterfaces = directlyImplementedInterfaces.flatMap {
        if (it.isLoaded) it.allImplementedInterfaces + it else listOf(it)
      }.toMutableSet()
      if (superType != null) allInterfaces.addAll(superType!!.allImplementedInterfaces)
      return allInterfaces
    }

  override fun withGenericTypes(genericTypes: List<JavaType>): JavaType {
    throw UnsupportedOperationException("Doesn't support generics for marcel classes (for now)")
  }

  override fun addImplementedInterface(javaType: JavaType) {
    directlyImplementedInterfaces.add(javaType)
  }
}

abstract class LoadedJavaType internal constructor(final override val realClazz: Class<*>, final override val genericTypes: List<JavaType>,
                              override val storeCode: Int, override val loadCode: Int, override val returnCode: Int): AbstractJavaType() {
  override val isLoaded = true
  override val descriptor: String
    get() = AsmUtils.getClassDescriptor(realClazz)

  override val className: String = realClazz.name
  override val superType get() =  if (realClazz.superclass != null) JavaType.of(realClazz.superclass) else null

  override val isAnnotation = realClazz.isAnnotation
  override val asPrimitiveType: JavaPrimitiveType
    get() = when(realClazz) {
      JavaType.Integer.realClazz -> JavaType.int
      JavaType.Long.realClazz -> JavaType.long
      JavaType.Character.realClazz -> JavaType.char
      JavaType.Float.realClazz -> JavaType.float
      JavaType.Double.realClazz -> JavaType.double
      JavaType.Boolean.realClazz -> JavaType.boolean
      else -> super.asPrimitiveType
    }

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

  override val packageName: String? = realClazz.`package`?.name
  constructor(realClazz: Class<*>): this(realClazz, emptyList())

  override fun withGenericTypes(genericTypes: List<JavaType>): JavaType {
    if (genericTypes == this.genericTypes) return this
    if (genericTypes.any { it.primitive }) throw MarcelSemanticException("Cannot have a primitive generic type")
    if (isLambda && genericTypes.size == realClazz.typeParameters.size - 1) {
      return LoadedObjectType(realClazz, genericTypes + JavaType.Object)
    }

    if (genericTypes.size != realClazz.typeParameters.size
      // for lambda, we can omit return type. It will be cast
      && !isLambda && genericTypes.size != realClazz.typeParameters.size - 1) throw MarcelSemanticException("Typed $realClazz expects ${realClazz.typeParameters.size} parameters")
    return LoadedObjectType(realClazz, genericTypes)
  }

  override fun raw(): JavaType {
    return LoadedObjectType(realClazz, emptyList())
  }
}

interface JavaArrayType: JavaType {
  val elementsType: JavaType
  val arrayStoreCode: Int
  val arrayLoadCode: Int
  val typeCode: Int
}

class NotLoadedJavaArrayType internal  constructor(
  override val elementsType: JavaType
): NotLoadedJavaType("[L${elementsType.className};", emptyList(), emptyList(), JavaType.Object, false, mutableSetOf()), JavaArrayType {
  override val arrayStoreCode: Int = Opcodes.AASTORE
  override val arrayLoadCode: Int = Opcodes.AALOAD
  override val typeCode: Int = 0

  override val asArrayType: JavaArrayType
    get() = this

  // TODO test this, it might only work for 1D arrays
  override val internalName: String
    get() = "[L" + AsmUtils.getInternalName(elementsType) + ";"
}


class LoadedJavaArrayType internal constructor(
  realClazz: Class<*>,
  override val elementsType: JavaType,
  override val arrayStoreCode: Int,
  override val arrayLoadCode: Int,
  override val typeCode: Int
): LoadedJavaType(realClazz, emptyList(), Opcodes.ASTORE, Opcodes.ALOAD, Opcodes.ARETURN), JavaArrayType {


  // constructor for non-primitive arrays
  constructor(realClazz: Class<*>): this(realClazz, JavaType.of(realClazz.componentType), Opcodes.AASTORE, Opcodes.AALOAD, 0)
  override val isArray get() = true
  override val packageName = null
  override val asArrayType: JavaArrayType
    get() = this

  override fun withGenericTypes(genericTypes: List<JavaType>): JavaType {
    throw MarcelSemanticException("Cannot have array type with generic types")
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
  val divCode: Int,
  override val defaultValueExpression: ExpressionNode): LoadedJavaType(objectKlazz.javaPrimitiveType!!, emptyList(), storeCode, loadCode, returnCode) {

    override val packageName = null
  val objectClass = objectKlazz.java
  override val objectType: JavaType
    get() = JavaType.of(objectClass)
  override fun withGenericTypes(genericTypes: List<JavaType>): JavaPrimitiveType {
    if (genericTypes.isNotEmpty()) throw MarcelSemanticException("Cannot have primitive type with generic types")
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

  override val arrayType: JavaArrayType
    get() = actualType.arrayType

  override val isAnnotation: Boolean
    get() = actualType.isAnnotation
  override val packageName: String?
    get() = actualType.packageName
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
  override val objectType: JavaType
    get() = actualType.objectType

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

  override fun toString(): String {
    return if (_actualType == null) className else actualType.toString()
  }
}