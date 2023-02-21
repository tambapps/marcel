# Functions

Use the `fun` keyword to define functions

```marcel
fun sum(int a, int b) int {
  return a + b
}
fun foo() {
  // do nothing
}
```

As shown in the above example, a function define has the following structures
1. starts with the [visibility](./visibility.md)  (you can also define static function with the `static` keyword)
2. the list of your function's parameters. The parameter's type first, and then the parameter's name.
3. the return type. If you're defining a void function, you can omit the return type.



## Function Visibility

You can specify your function's [visibility](./visibility.md) before the `fun` keyword

```kotlin
private fun foo() {
}
```

## Parameter default value (Not Yet Implemented)
Function parameters can have default values, which are used when you skip the corresponding argument.

```kotlin
fun sum(int a = 0, int b = 8) int {
return a + b
}

sum(a = 2) // 2 + 8
sum(b = 5) // 0 + 8
sum(a = 2, b = 5) // 2 + 5
```


## Function Calls
Function calls are no different than  in any other language

```groovy
int result = sum(1, 2)
```

### Named Parameters Call
You can also specify the name of your parameters. When doing so, the order in which
you specify them doesn't matter.

When you don't specify a parameter, it will default to the type's default value
(0 for primitive types and `null` for Objects).

Following on our `sum()` example:
```groovy
int result = sum(b: 2, a: 1) // equivalent to sum(1, 2)
int otherResult = sum(a: 1) // equivalent to sum(1, 0)
```


Such calls also work with constructors. You can specify class's field names with their values to set.
Note that it will only work if your class has a no-arg constructor and that the fields referenced are `public`



```groovy

Foo foo = new Foo(bar: 1, baz: "baz")

class Foo {
  int bar
  String baz
}
```