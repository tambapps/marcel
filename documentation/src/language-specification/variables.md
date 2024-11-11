# Variables

## Declare variables

Marcel variable declarations are the same as Java's

````marcel
int a = 2
Object o = new Object()

List<int> l = [1, 2] // collection of primivites
````

### Multiple declarations
Marcel supports multiple assignments in one statement

```marcel
def (int a, String b, Object c) = [1, "2", new Object()]

def (int d, String e, Object f) = functionReturningAnArrayOrList()
```

Note that if the array/list is shorter than the number of variable declared, this will lead to a runtime error

<br/>

Sometimes you might want to ignore a specific item of a list,
You can use the `_` identifier to let the compiler know that.

E.g.

```marcel
def (_, String world) = ("hello world" =~ /hello (world)/).groups()
```

## Variable assignments

Just use `=` to assign values to defined variables

```marcel
int a = 2

a = 3
```

## Properties
Marcel allows to access getters and setters as properties.

Suppose you have the below class

```marcel
class Foo {
  private int bar
  
  fun getBar() {
    return this.@bar
  }
  
  fun setBar(int bar) {
    this.@bar = bar
  }
}
```

You could call these `getBar/setBar` methods using the property syntax.
The `@bar` notation is the [direct field access operator](./operators/direct-field-access.md), make sure to reference the Java class's field.

```marcel
Foo foo = new Foo()

foo.bar = 5 // will actully call foo.setBar(5)
println(foo.bar) // will actually call foo.getBar()

```

### Automatic casting
Variable assignments are automatically casted when needed.

```marcel
Optional o = Optional.of(123)
Integer myInteger = o.get() // returns Object cast into an Integer 
int myInt = o.get() // returns Object cast into an Integer then into an int
```

This can be useful as Marcel [doesn't support generic types](./types/index.md#generic-types).