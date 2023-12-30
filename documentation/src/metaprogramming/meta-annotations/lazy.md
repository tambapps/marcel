# @lazy

This annotation is used to make a field lazy. The value of the field will only be computed when it is referenced, and 
not before.

## Prerequisites

The annotated field must be of Object type (non-primitive) and must have an initial value specified.

E.g.

```marcel
@lazy
Integer i = computeI()

fun int computeI() {
  initCount++
  return 1
}
```

## How it is transformed

This meta-annotation will transform the code to make the annotated field lazy.

Using the above example, the code would be transformed as such

```marcel
private Integer _i = null

fun getI() {
  if (_i == null) {
    _i = computeI()
  }
  return _i
}

fun int computeI() {
  initCount++
  return 1
}
```
As Marcel allows to [access getters as properties](../../language-specification/variables.md#properties), you can access this variable using
the `a` syntax (or `this.a`), as if you were referencing the original field.
You'll notice that in the above script, the value of `i` will never be computed, as the variable `i` wasn't referenced anywhere outside.

Now let's take a look at a full example.

```marcel
@lazy
Integer i = computeI()

for (int _ in 1..3) println(i)

fun int computeI() {
  initCount++
  return 1
}
```

The output of the above script would be

```text
Computing...
1
1
1
```

As the variable is lazy, the value of i will be computed at the first call of `println(i)`, and the other calls
will just use the already computed value.