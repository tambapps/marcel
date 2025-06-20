# While loops
While loops are also similar as Java's. You can also provide any expression in them as you would in a Marcel [if](./if-else.md).
If the provided expression is not a boolean, the [Marcel truth](marcel-truth.md) will decide if your expression is `true` or not.

```marcel
int i = 0
while (i < 10) {
  println(i++)
}
```

## while variable declaration
The [marcel truth](marcel-truth.md) allows you to declare variable in an `while` condition, and execute the code block if the variable is truthy
```marcel
while (String line = reader.readLine()) println(line)

// outside the loop, this line variable doesn't exist anymore
```

# Do while
You can also perform do-while instructions, which will always execute at least one the do statement and then check the condition.
If the condition is true the do statement is executed again.

```marcel
int i = 15
do {
  println(i++)
} while (i < 10)
```

The above code will only print `15`.

# Do
You can specify a `do` instruction without a while instruction at the end. It will just execute the do statement once.
This can be useful to create inner scopes, in which you can create all the local variables you want, as they won't be accessible
outside the `do` scope

```marcel

int result = 5
do {
  int variable = 1
  int anotherVariable = 2
  int anotherOtherVariable = 3
  int anotherOne = 5
  result = variable + anotherVariable + anotherOtherVariable + anotherOne
}

// now only result variable exists
```