package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.type.JavaType
import java.util.regex.Pattern

class IntConstantNode(token: LexToken = LexToken.dummy(), val value: Int): AbstractExpressionNode(token) {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return value.toString()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as IntConstantNode

    if (value != other.value) return false

    return true
  }

  override fun hashCode(): Int {
    return value
  }
}

class ByteConstantNode(token: LexToken = LexToken.dummy(), val value: Byte): AbstractExpressionNode(token) {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return value.toString()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ByteConstantNode

    if (value != other.value) return false

    return true
  }

  override fun hashCode(): Int {
    return value.hashCode()
  }
}

class ShortConstantNode(token: LexToken = LexToken.dummy(), val value: Short): AbstractExpressionNode(token) {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return value.toString()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ShortConstantNode

    if (value != other.value) return false

    return true
  }

  override fun hashCode(): Int {
    return value.hashCode()
  }
}

class LongConstantNode(token: LexToken = LexToken.dummy(), val value: Long): AbstractExpressionNode(token) {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return value.toString()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as LongConstantNode

    if (value != other.value) return false

    return true
  }

  override fun hashCode(): Int {
    return value.hashCode()
  }
}

class FloatConstantNode(token: LexToken = LexToken.dummy(), val value: Float): AbstractExpressionNode(token) {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return value.toString()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as FloatConstantNode

    if (value != other.value) return false

    return true
  }

  override fun hashCode(): Int {
    return value.hashCode()
  }
}

class DoubleConstantNode(token: LexToken = LexToken.dummy(), val value: Double): AbstractExpressionNode(token) {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return value.toString()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as DoubleConstantNode

    if (value != other.value) return false

    return true
  }

  override fun hashCode(): Int {
    return value.hashCode()
  }
}

class StringConstantNode(token: LexToken = LexToken.dummy(), val value: String): AbstractExpressionNode(token) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return "\"$value\""
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as StringConstantNode

    if (value != other.value) return false

    return true
  }

  override fun hashCode(): Int {
    return value.hashCode()
  }
}


class LiteralPatternNode(token: LexToken = LexToken.dummy(), val value: String, val flags: List<Int>): AbstractExpressionNode(token) {

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

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as LiteralPatternNode

    if (value != other.value) return false
    if (flags != other.flags) return false

    return true
  }

  override fun hashCode(): Int {
    var result = value.hashCode()
    result = 31 * result + flags.hashCode()
    return result
  }
}


class BooleanConstantNode(token: LexToken = LexToken.dummy(), val value: Boolean): AbstractExpressionNode(token) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as BooleanConstantNode

    if (value != other.value) return false

    return true
  }

  override fun hashCode(): Int {
    return value.hashCode()
  }

}

class NullValueNode(token: LexToken = LexToken.dummy(), var type: JavaType?): AbstractExpressionNode(token) {

  constructor(token: LexToken = LexToken.dummy()): this(token, null)
  constructor(): this(LexToken.dummy(), null)

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

class CharConstantNode(token: LexToken = LexToken.dummy(), val value: String): AbstractExpressionNode(token) {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return "`$value`"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as CharConstantNode

    if (value != other.value) return false

    return true
  }

  override fun hashCode(): Int {
    return value.hashCode()
  }

}