# Marcel In Action

Here are some use-cases of Marcel.

## Advent of code

Checkout [Tambapps's Advent Of Code](https://github.com/tambapps/advent-of-code)

## Fibonacci suite

Here is an implementation of the Fibonacci suite in Marcel.

```marcel
println(fibonacci(10))

fun int fibonacci(int n) {
  return switch (n) {
    0, 1 -> n
    else -> fibonacci(n - 1) + fibonacci(n - 2)
  }
}
```

But this implementation takes a lot of time when using large `n` values. A way to solve this problem would be to cache
fibonnaci's results, which can be done using the [@cached annotation](./metaprogramming/meta-annotations.md#cached)

```marcel
println(fibonacci(10))

@cached
fun int fibonacci(int n) -> switch (n) {
  0, 1 -> n
  else -> fibonacci(n - 1) + fibonacci(n - 2)
}
```