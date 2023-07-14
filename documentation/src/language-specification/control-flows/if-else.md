# If expression
If statements are similar as Java's, but you can provide any expression in them. If the provided expression is not a boolean,
the [Marcel truth](marcel-truth.md) will decide if your expression is `true` or not.

```kotlin
if (a == 1) {
  println("a is 1")
} else if (a == 2) {
  println("a is 2")
} else {
  println("a is not 1 and not 2")
}
```

## if variable declaration
The [marcel truth](marcel-truth.md) allows you to declare variable in an `if` condition, and execute the code block if the variable is truthy
```kotlin
if (Something result = fetchSomething()) {
  println("Fetched $result")
}
```

You can also unbox Optional values such as in the below example

```kotlin
// assuming getOptionalInteger() returns an Optional
if (Integer result = getOptional()) {
  println(result)
}
```