package com.tambapps.marcel.semantic.processor.scope

import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.symbol.method.MarcelMethod
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import marcel.util.concurrent.Threadmill

/**
 * Scope of an async block. It extends an inner scope because
 * the method running the async block isn't async itself (it doesn't particularly return a Threadmill future)
 * but the code inside it is safe to use async features
 *
 *
 * @param symbolResolver the marcel symbol resolver
 * @param method the generated method that will run the async block
 * @param originalScope the scope in which the async block was declared
 */
class AsyncScope(symbolResolver: MarcelSymbolResolver, method: MarcelMethod, originalScope: MethodScope) :
  MethodInnerScope(
    MethodScope(
      ClassScope(
        symbolResolver, method.ownerClass, null,
        // await methods are available in an async block
        importResolver = originalScope.importResolver.plus(staticMemberImports = mapOf(Pair("await", Threadmill::class.javaType)))),
      method), isInLoop = false, isAsync = true
  )