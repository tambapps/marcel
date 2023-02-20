package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.type.JavaType
import java.util.regex.Pattern

data class IntConstantNode(val value: Int): ExpressionNode {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return value.toString()
  }
}

data class ByteConstantNode(val value: Byte): ExpressionNode {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return value.toString()
  }
}

data class ShortConstantNode(val value: Short): ExpressionNode {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return value.toString()
  }
}

data class LongConstantNode(val value: Long): ExpressionNode {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return value.toString()
  }
}

data class FloatConstantNode(val value: Float): ExpressionNode {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return value.toString()
  }
}

data class DoubleConstantNode(val value: Double): ExpressionNode {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return value.toString()
  }
}

data class StringConstantNode(val value: String): ExpressionNode {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return "\"$value\""
  }
}


data class LiteralPatternNode(val value: String, val flags: List<Int>): ExpressionNode {

  companion object {
    val FLAGS_MAP = mapOf(
      Pair('d', Pattern.UNIX_LINES),
      Pair('i', Pattern.CASE_INSENSITIVE),
      Pair('x', Pattern.COMMENTS),
      Pair('m', Pattern.MULTILINE),
      Pair('l', Pattern.LITERAL),
      Pair('s', Pattern.DOTALL),
      Pair('u', Pattern.UNICODE_CASE),
      Pair('c', Pattern.CANON_EQ),
      Pair('U', Pattern.UNICODE_CHARACTER_CLASS),
    )
  }
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    val flagsString = if (flags.isEmpty()) "" else ", " + flags.reduce{ a: Int, b: Int -> a or b }
    return "Pattern.compile(\"$value\"$flagsString)"
  }
}


data class BooleanConstantNode(val value: Boolean): ExpressionNode {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

}

class NullValueNode(var type: JavaType?): ExpressionNode {

  constructor(): this(null)

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun equals(other: Any?): Boolean {
    return other is NullValueNode
  }

  override fun hashCode(): Int {
    return 0
  }

  override fun toString(): String {
    return if (type != null) "($type) null" else "null"
  }
}

class CharConstantNode(val value: String): ExpressionNode {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return "`$value`"
  }

}