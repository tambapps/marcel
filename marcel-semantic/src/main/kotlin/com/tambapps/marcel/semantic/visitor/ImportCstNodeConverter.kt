package com.tambapps.marcel.semantic.visitor

import com.tambapps.marcel.parser.cst.imprt.ImportCstNode as ImportCstNode
import com.tambapps.marcel.parser.cst.imprt.ImportCstVisitor
import com.tambapps.marcel.parser.cst.imprt.SimpleImportCstNode as SimpleImportCstNode
import com.tambapps.marcel.parser.cst.imprt.StaticImportCstNode as StaticImportCstNode
import com.tambapps.marcel.parser.cst.imprt.WildcardImportCstNode as WildcardImportCstNode
import com.tambapps.marcel.semantic.ast.ImportNode
import com.tambapps.marcel.semantic.ast.SimpleImportNode
import com.tambapps.marcel.semantic.ast.StaticImportNode
import com.tambapps.marcel.semantic.ast.WildcardImportNode

object ImportCstNodeConverter: ImportCstVisitor<ImportNode> {

  fun convert(importCstNodes: List<ImportCstNode>): List<ImportNode> = importCstNodes.map(this::convert)

  fun convert(importCstNode: ImportCstNode) = importCstNode.accept(this)

  override fun visit(node: SimpleImportCstNode) = SimpleImportNode(node.className, node.asName, node.tokenStart, node.tokenEnd)

  override fun visit(node: StaticImportCstNode) = StaticImportNode(node.className, node.methodName, node.tokenStart, node.tokenEnd)

  override fun visit(node: WildcardImportCstNode) = WildcardImportNode(node.prefix, node.tokenStart, node.tokenEnd)

}