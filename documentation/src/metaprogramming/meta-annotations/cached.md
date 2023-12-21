# @cached

This annotation allows to cache results of a method in order to prevent having to compute multiple times the result for a same input.
It will generate a cache and make the annotated method use this cache. The cache key is produced with the method parameter(s).

If the value for the given input (method parameters) is in the cache, we just return it from the cache. If it isn't in it, we compute the value, put
it in the cache and then return it.


A perfect example is a recursive implementation of the fibonacci suite. With high numbers, we may compute multiple times the value
for a same input, making the operation really long. That's where `@cached` comes to save the day. No value for a same input will be computed more than once, this
will allow to save a lot of time.

```marcel
println(fibonacci(10))

@cached
fun int fibonacci(int n) -> switch (n) {
  0, 1 -> n
  else -> fibonacci(n - 1) + fibonacci(n - 2)
}
```

This script would print `Foo(bar=myBar)`. 

Caching also work with functions having many parameters

```marcel
println(funnynacci(10, 15))

@cached
fun int funnynacci(int n, int m) -> switch (n) {
  0, 1 -> n + m
  else -> funnynacci(n - 1, m - 1) + funnynacci(n - 2, m - 1)
}
```
## Thread-safe cache
By default, the cache is **not** thread safe and is backed by a [HashMap](https://docs.oracle.com/javase/8/docs/api/java/util/HashMap.html).

You can set the `threadSafe` flag to `true` if you want to make the implementation thread-safe. The cache will then be backed by 
a [ConcurrentHashMap](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentHashMap.html).

```marcel
println(fibonacci(10))

@cached(threadSafe=true)
fun int fibonacci(int n) -> switch (n) {
  0, 1 -> n
  else -> fibonacci(n - 1) + fibonacci(n - 2)
}
```