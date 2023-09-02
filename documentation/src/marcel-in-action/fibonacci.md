# Fibonacci suite

Here is an implementation of the Fibonacci suite in Marcel.

```kotlin
println(fibonacci(10))

fun int fibonacci(int n) {
  return switch (n) {
    0, 1 -> n
    else -> fibonacci(n - 1) + fibonacci(n - 2)
  }
}
```

But this implementation takes a lot of time when using large `n` values. A way to solve this problem would be to cache
fibonnaci's results, which can be done using the `@Cached` annotation (Not Yet Implemented)

```kotlin
println(fibonacci(10))

@Cached
fun int fibonacci(int n) {
  return switch (n) {
    0, 1 -> n
    else -> fibonacci(n - 1) + fibonacci(n - 2)
  }
}
```