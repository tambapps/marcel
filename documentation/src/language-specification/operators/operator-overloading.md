# Define custom operators
You can define operators in a very similar way as [groovy's](https://groovy-lang.org/operators.html#Operator-Overloading).

Each operator is associated to a function. To define an operator for a given type.

Here is the table of functions to define for each operator


## Define operators from extensions


| Operator   | Method            |
|------------|-------------------|
| a + b      | a.plus(b)         |
| a - b      | a.minus(b)        |
| - a        | a.negate()        |
| a * b      | a.multiply(b)     |
| a % b      | a.mod(b)          |
| a / b      | a.div(b)          |
| a << b     | a.leftShift(b)    |
| a \>> b    | a.rightShift(b)   |
| a[b]       | a.getAt(b)        |
| a?[b]      | a.getAtSafe(b)    |
| a[b, c, d] | a.getAt(b, c, d)  |
| a[b] = c   | a.putAt(b, c)     |
| a?[b] = c  | a.putAtSafe(b, c) |
