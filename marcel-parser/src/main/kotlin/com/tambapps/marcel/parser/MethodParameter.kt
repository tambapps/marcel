package com.tambapps.marcel.parser

import com.tambapps.marcel.parser.ast.AstTypedObject
import com.tambapps.marcel.parser.type.JavaType

/*
 Note to self. Normally method implementing a generic interface would be compiled the following way with 2 methods.

  public void accept(java.lang.Integer);
    Code:
       0: getstatic     #7                  // Field java/lang/System.out:Ljava/io/PrintStream;
       3: aload_1
       4: invokevirtual #13                 // Method java/io/PrintStream.println:(Ljava/lang/Object;)V
       7: return

  public void accept(java.lang.Object);
    Code:
       0: aload_0
       1: aload_1
       2: checkcast     #19                 // class java/lang/Integer
       5: invokevirtual #21                 // Method accept:(Ljava/lang/Integer;)V
       8: return

  But what I do (for now) is compiling only one method with the raw type (Object) and then cast to the generic type
 */
data class MethodParameter constructor(override val type: JavaType, val rawType: JavaType, val name: String): AstTypedObject {
  constructor(type: JavaType, name: String): this(type, type, name)

  val rawVarName get() = if (rawType != type) "_rawArg_${name}" else name
  override fun toString(): String {
    return "$type $name"
  }
}