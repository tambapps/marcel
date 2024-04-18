# Chain operations
You can chain multiple iterable operations in a same expression using the right shift (`>>`) operator.

```marcel
list<int> list = [1, 2, 3, 4]
println(list >> when int a -> a % 2 == 0)
println([for int a in list -> a + 3] >> when int a -> a % 2 == 0)
```

In those example you can see that we omit the `in something` part of the operations, this is because the left operand
of the `>>` operator is used instead.

So the above code is equivalent of the below code.

```marcel
list<int> list = [1, 2, 3, 4]
println(when int a in list -> a % 2 == 0)
println(when int a in [for int a in list -> a + 3] -> a % 2 == 0)
```
But this piece of code is hard to read, right? That is why the `>>` is here. 