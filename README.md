# marcel
Marcel is language that compiles to Java bytecode, with a guaranteed support for Android. It also aims to use primitive types as much as possible when
working with Collections. For that, it uses the [fastutil library](https://github.com/vigna/fastutil), but Marcel will (soonish) have it's own implementation.

You can learn more about this programming language on the [wiki page](https://github.com/tambapps/marcel/wiki)
## Example

Here is an example on how to code the Fibonnaci suite in Marcel

```kotlin
println(fibonnaci(10))

fun fibonacci(int n) int {
  if (n == 0) return 0
  else if (n == 1 || n == 2) return 1
  else return fibonacci(n - 1) + fibonacci(n - 2)
}
```
