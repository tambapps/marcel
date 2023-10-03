package com.tambapps.marcel.parser.cst.expression.reference

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode

class IncrCstNode(
    parent: CstNode?,
    override val value: String,
    val amount: Int,
    val returnValueBefore: Boolean,
    tokenStart: LexToken, tokenEnd: LexToken) : CstNode(parent, tokenStart, tokenEnd) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as IncrCstNode

        if (value != other.value) return false
        if (amount != other.amount) return false
        if (returnValueBefore != other.returnValueBefore) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + amount
        result = 31 * result + returnValueBefore.hashCode()
        return result
    }
}