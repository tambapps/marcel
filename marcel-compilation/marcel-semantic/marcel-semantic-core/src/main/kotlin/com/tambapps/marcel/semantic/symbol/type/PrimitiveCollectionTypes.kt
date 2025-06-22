package com.tambapps.marcel.semantic.symbol.type

import com.tambapps.marcel.semantic.symbol.type.JavaType.Companion.PRIMITIVE_LIST_MAP
import com.tambapps.marcel.semantic.symbol.type.JavaType.Companion.PRIMITIVE_SET_MAP

object PrimitiveCollectionTypes {

  fun fromArrayType(javaType: JavaArrayType) = when (javaType) {
    JavaType.intArray -> JavaType.intList
    JavaType.longArray -> JavaType.longList
    JavaType.floatArray -> JavaType.floatList
    JavaType.doubleArray -> JavaType.doubleList
    JavaType.charArray -> JavaType.charList
    else -> null
  }

  fun listFromPrimitiveType(javaType: JavaPrimitiveType) = PRIMITIVE_LIST_MAP[javaType]

  fun setFromPrimitiveType(javaType: JavaPrimitiveType) = PRIMITIVE_SET_MAP[javaType]

  fun hasPrimitiveCollection(javaType: JavaPrimitiveType) = PRIMITIVE_LIST_MAP.containsKey(javaType)

}