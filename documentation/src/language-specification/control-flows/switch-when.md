# Switch

Marcel define switches, inspired from kotlin

```marcel
switch (string) {
  "foo" -> 2
  "bar" -> 3
}
```

In marcel (and like in Kotlin), **switches return values**

In the above example, you can notice that a default case is missing.
If nothing matched the provided expression, the switch will return `null`.

This means that you must **always** specify an `else` branch for switch returning primitive types, as they cannot be null.

```marcel
int myInt = switch (c) {
  1 -> 2
  2 -> 3
  3 -> 4
  else -> 5
}
```

## Access the switched expression
The provided expression can be accessed in the switch branches using the implicit variable `it`.

```marcel
int myInt = switch (operator) {
  1 -> it + 1
  else ->  it + 4
}
```