# marcel
JVM-based programming language guaranteed to work on Android.

## Specifications

### Types
It supports the following primitive types
- int
- long
- float
- double
- boolean
- void

### Functions (on-going)

here is the syntax to define a function

```
private fun foo(arg1 int, arg2 int) int {

}

// void function
private fun bar() {

}
```

Functions are public by default

### Visibility

We have the `public`, `protected`, `private` and `hidden` (package-private) visibility.