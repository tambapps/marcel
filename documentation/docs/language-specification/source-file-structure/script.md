# Write Scripts

Scripts don't need a main() function. You can just start writing statements of your script directly, without wrapping them in a method.
Script can be executed easily with [MarCL](../../tools/marcl.md).

<br/>

You can also define functions in your scripts.

E.g.

```marcel
println(fibonacci(10))

@cached
fun int fibonacci(int n) -> switch (n) {
  0, 1 -> n
  else -> fibonacci(n - 1) + fibonacci(n - 2)
}
```

## Local Variables

To declare a local variable in a script, simply declare it as you would in a function's body.

```marcel
int a = 2
int b
```

## Fields

To declare a class field for your script, you must explicitly provide its visibility, otherwise it will be 
considered as a local variable.

E.g.

```marcel
internal int myField1 = 2
protected myfield2
```


## Global variables
Global variables are similar to fields. They were created especially for [Marshell](../../tools/marshell.md), in which you can't
declare fields.

## Classes

You can also define classes in a script, but note that such classes will **not** be an inner class of your script. They will be 
top-level classes.