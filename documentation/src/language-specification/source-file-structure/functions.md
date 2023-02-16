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



## Function visibility

You can specify your function's [visibility](./visibility.md) before the `class` keyword

```kotlin
public class Foo {

}
```

## Function named parameters (Not Yet Implemented)
Function arguments can have default constant values You can specify method argument names when calling a function. 

Following on our `sum()` example:
```kotlin
int result = sum(a = 1, b = 2)
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