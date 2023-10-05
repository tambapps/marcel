package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.parser.cst.expression.literral.DoubleCstNode
import com.tambapps.marcel.parser.cst.expression.literral.FloatCstNode
import com.tambapps.marcel.parser.cst.expression.literral.IntCstNode
import com.tambapps.marcel.parser.cst.expression.literral.LongCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ClassReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.DirectFieldReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.IncrCstNode
import com.tambapps.marcel.parser.cst.expression.reference.IndexAccessCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ReferenceCstNode

interface StatementCstNodeVisitor<T> {

  fun visit(node: ExpressionStatementCstNode): T

}