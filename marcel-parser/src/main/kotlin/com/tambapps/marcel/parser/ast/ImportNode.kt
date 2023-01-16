package com.tambapps.marcel.parser.ast

open class ImportNode(val value: String, val asName: String? = null): AstNode

class WildcardImportNode(value: String): ImportNode(value, null) {

}