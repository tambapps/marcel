package com.tambapps.marcel.semantic.scope

import com.tambapps.marcel.semantic.ast.ImportNode
import com.tambapps.marcel.semantic.ast.StaticImportNode
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver
import com.tambapps.marcel.threadmill.Threadmill

/**
 * Scope of an async block. It extends an inner scope because
 * the method running the async block isn't async itself (it doesn't particularly return a Threadmill future)
 * but the code inside it is safe to use async features
 *
 *
 * @param symbolResolver the marcel symbol resolver
 * @param method the generated method that will run the async block
 * @param parentScope the parent scope
 */
class AsyncScope(symbolResolver: MarcelSymbolResolver, method: JavaMethod, imports: List<ImportNode>) :
  MethodInnerScope(
    MethodScope(
      ClassScope(symbolResolver, method.ownerClass, null,
        // await methods are available in an async block
        imports = imports + listOf<ImportNode>(
          StaticImportNode(Threadmill::class.qualifiedName!!, "await")
        )
      ), method)
    , isInLoop = false, isAsync = true) {
}