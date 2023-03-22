# marcel
Marcel is language that compiles to Java bytecode, with a guaranteed support for Android. It also aims to use primitive types as much as possible when
working with Collections.

You can learn more about this programming language on [its website](https://tambapps.github.io/marcel)
## Example

Here is an example on how to code the Fibonacci suite in Marcel

```kotlin
println(fibonacci(10))

fun fibonacci(int n) int {
  if (n == 0) return 0
  else if (n == 1 || n == 2) return 1
  else return fibonacci(n - 1) + fibonacci(n - 2)
}
```
