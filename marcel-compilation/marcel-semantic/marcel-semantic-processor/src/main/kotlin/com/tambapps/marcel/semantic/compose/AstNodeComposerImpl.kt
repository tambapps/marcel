package com.tambapps.marcel.semantic.compose

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.MarcelSemanticGenerator
import com.tambapps.marcel.semantic.ast.cast.AstNodeCaster
import com.tambapps.marcel.semantic.scope.Scope
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver
import java.util.*

internal class AstNodeComposerImpl(
  override val symbolResolver: MarcelSymbolResolver,
  override val caster: AstNodeCaster,
  scopeQueue: LinkedList<Scope>,
  tokenStart: LexToken,
  tokenEnd: LexToken,
) : AstNodeComposer(tokenStart, tokenEnd, scopeQueue) {

  constructor(semantic: MarcelSemanticGenerator,tokenStart: LexToken,
              tokenEnd: LexToken,): this(semantic.symbolResolver, semantic.caster, semantic.scopeQueue, tokenStart, tokenEnd)
}