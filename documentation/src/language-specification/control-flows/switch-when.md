# Switch and When

Marcel defines conditional control flows that can return values. Note that in the below
control flows described, you **can't** re-assign a value to **local variables** created **outside** 
the switch/when scope.

## When

```marcel
when {
  string == "foo" -> 2
  string == "bar" -> {
    doSomeStuff()
    3
  }
  otherCondition() -> 4
}
```

Each `when` branch consists of a condition, and a statement. If the given condition is true, the corresponding statement
will be executed.

Whens are very similar to `if/elseif/else` control flow, but they allow you to **return values**. 
They are useful to assign variables, or returning values in functions.


In the above example, you can notice that a default case is missing.
If no conditions matched, the `when` will return `null`.

This means that you must **always** specify an `else` branch for whens returning primitive types, as they cannot be null.

```marcel
int myInt = when {
  string == "foo" -> 2
  string == "bar" -> {
    doSomeStuff()
    3
  }
  string == someString() -> 4
  otherCondition() -> 5
  else -> 5
}
```


## Switch

Switch are very similar to whens. Every switch can be translated to a when (but the other way around is not true).

In switches, you compare an expression against multiple values. Based on the above `when` example, we could do the following
`switch`

```marcel
switch (string) {
  "foo" -> 2
  "bar" -> {
    doSomeStuff()
    3
  }
  someString() -> 4
}
```

You'll notice that we couldn't translate the `when` condition `otherCondition()`, this is because it isn't a comparison against
the switched expression.


Each switch branch consists of a value, and a statement. If the provided switch expression matches the branch's expression, 
the corresponding statement will be executed.

Switches also have an else, that is required when returning a primitive

```marcel
int myInt = switch (string) {
  "foo" -> 2
  "bar" -> {
    doSomeStuff()
    3
  }
  someString() -> 4
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