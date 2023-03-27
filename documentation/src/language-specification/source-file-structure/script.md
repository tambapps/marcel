# Write Scripts

Scripts don't need a main() function. You can just start writing statements of your script directly, without wrapping them in a method.

<br/>
You can also define functions in your scripts.

## Local Variables

To declare a local variable in a script, simply declare it as you would in a function's body.

```groovy
int a = 2
int b
```

## Dynamic variables
In scripts, you can use variables that are declared automatically if they don't
exist yet.

```groovy
a = 2
println(a)
```
In the above script, we didn't declare the variable `a`. If we were in a regular class this wouldn't compile, but in scripts
it would declare the variable.

Such variables are declared automatically when they are first assigned. This is why the below script wouldn't compile
```groovy
println(a) // referencing a variable a, but we didn't assign it before => semantic error
a = 2
```

The type of the variable is determined by the value provided
to assign it. To explicitly specify its type, you can use the `as` keyword like in the below example

```groovy
a = 2 as Integer // declaring a global variable Integer a
println(a)
```

Global variables are accessible from anywhere in the script context, as long as it is not static.

### Type consistency
Marcel is statically typed, and so are global variables (at least they are considered as so by the compiler).
When you assign a value to a global variable, future assignments must provide a value assignable from the first value used
in the first assignment.

### How global variables works
Global variables are variables that are stored in the script's `Binding`. The means you could also
retrieve them/set them using methods like `Script.getVariable(name)`/`Script.setVariable(name, value)`

```groovy
a = 1

doSomething(a)

a = "2" // Semantic Error: Expected expression of type int but gave String
```


## Fields

To declare a class field for your script, you must explicitly provide its visibility, otherwise it will be 
considered as a local variable.

E.g.

```groovy
internal int myField1 = 2
protected myfield2
```

Note that global variables are a lot like field variables so you don't need to use both. Just use global variables or field variables
based on your preferences.

## (Inner) Classes

You can also define classes in a script, but note that such classes will be an inner class (its outer class being the script's class)
