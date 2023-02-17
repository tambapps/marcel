# Write Scripts

Scripts don't need a main() function. You can just start writing statements of your script directly, without wrapping them in a method.

<br/>
You can also define functions in your scripts.

## Local Variables

To declare a variable in a script, simply declare it as you would in a function's body.

```groovy
int a = 2
int b
```

## Fields

To declare a class field for your script, you must explicitly provide its visibility, otherwise it will be 
considered as a local variable.

E.g.

```groovy
internal int myField1 = 2
protected myfield2
```

## (Inner) Classes

You can also define classes in a script, but note that such classes will be an inner class (its outer class being the script's class)
