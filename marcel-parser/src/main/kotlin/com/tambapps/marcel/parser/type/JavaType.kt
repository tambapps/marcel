package com.tambapps.marcel.parser.type

import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.PrimitiveTypes
import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.ast.TypedNode
import org.objectweb.asm.Opcodes

// TODO add an imple for dynamic types e.g. class defined in a marcel script
open class JavaType(
  val realClassOrObject: Class<*>,
    val className: String,
    val internalName: String,
    val descriptor: String,
    val storeCode: Int,
    val loadCode: Int,
    val returnCode: Int): TypedNode {

  override val type: JavaType get() = this
  open val primitive = false

  constructor(clazz: Class<*>): this(clazz, clazz.name, AsmUtils.getInternalName(clazz), AsmUtils.getClassDescriptor(clazz),
  Opcodes.ASTORE, Opcodes.ALOAD, Opcodes.ARETURN)

  // constructors for class defined in a script
  constructor(clazz: String): this(OBJECT.realClassOrObject, clazz, AsmUtils.getInternalName(clazz), AsmUtils.getObjectClassDescriptor(clazz), Opcodes.ASTORE, Opcodes.ALOAD, Opcodes.ARETURN)
  companion object {

    val OBJECT = JavaType(Object::class.java)
    val STRING = JavaType(String::class.java)


    val void = JavaPrimitiveType(PrimitiveTypes.VOID, Opcodes.ALOAD, Opcodes.ASTORE, Opcodes.RETURN, 0,0,0,0)
    val int = JavaPrimitiveType(PrimitiveTypes.INT, Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN, Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL, Opcodes.IDIV)
    val long = JavaPrimitiveType(PrimitiveTypes.LONG, Opcodes.LLOAD, Opcodes.LSTORE, Opcodes.LRETURN, Opcodes.LADD, Opcodes.LSUB, Opcodes.LMUL, Opcodes.LDIV)
    val float = JavaPrimitiveType(PrimitiveTypes.FLOAT, Opcodes.FLOAD, Opcodes.FSTORE, Opcodes.FRETURN, Opcodes.FADD, Opcodes.FSUB, Opcodes.FMUL, Opcodes.FDIV)
    val double = JavaPrimitiveType(PrimitiveTypes.DOUBLE, Opcodes.DLOAD, Opcodes.DSTORE, Opcodes.DRETURN, Opcodes.DADD, Opcodes.DSUB, Opcodes.DMUL, Opcodes.DDIV)
    // apparently we use int instructions to store booleans
    val boolean = JavaPrimitiveType(PrimitiveTypes.BOOL, Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN,  0,0,0,0)

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
    if (this == javaType || this == OBJECT
      // to handle null values that can be cast to anything
      || !primitive && javaType == void) {
      return true
    }
    if (primitive || javaType.primitive) {
      return this == javaType
    }
    // TODO only handle classes already defined, not parsed class
    val thisClass = Class.forName(className)
    val otherClass = Class.forName(javaType.className)
    return thisClass.isAssignableFrom(otherClass)
  }
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is JavaType) return false
    return className == other.className
  }

  override fun hashCode(): Int {
    return className.hashCode()
  }

  override fun toString(): String {
    return className
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
                        val divCode: Int): JavaType(realClassOrObject, className, internalName, descriptor, storeCode, loadCode, returnCode) {
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

}