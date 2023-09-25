# Instance Of (type checking)

The `instanceof` keyword allows to verify if an Object variable is **an instance** of the provided type.

It cannot be used on primitive variables, and will always return `false` on `null` variables.


## Examples

```marcel
Integer a = 1

println(a instanceof Integer) // true
println(a instanceof Number) // true
println(a instanceof Long) // false
```