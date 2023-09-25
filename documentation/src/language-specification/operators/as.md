# As (type casting)

The `as` keyword allows to cast variables to a provided type.


```marcel
int a = 1

Integer b = a as Integer
Number c = b as Number
Long d = c as Long // will fail as a is not an instance of Long
```