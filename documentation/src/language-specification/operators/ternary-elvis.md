# Ternary operator (<expr> ? <expr> : <expr>)

Marcel supports ternary operator like in Java. This operator have 3 operands:
1. the condition expression
2. the 'true' expression
3. the 'false' expression.

This operator evaluates the condition. It can be any kind of expression, as the [Marcel truth](../control-flows/marcel-truth.md) will determine if the expression
is truthy or not, for non-boolean expressions.

If the condition expression is truthy, the 'true' expression will be evaluated, otherwise it will be the 'false' expression.

E.g.

```marcel
int temperature = isSunny() ? 21 : -5
println(temperature)

fun bool isSunny() -> return true
```
This script will print the value `21`

Let's take a look at another example.

```marcel
Integer input = null
Integer a = input ? input : 34
println(a)
```
This script will print the value `34`

Note that this last example can be simplified using the **Elvis operator**


## Elvis operator

The Elvis operator is just a simplified ternary operator in which the condition expression and the 'true' expression
are the same.

You could translate the above example using the below code
```marcel
Integer input = null
Integer a = input ?: 34
println(a)
```