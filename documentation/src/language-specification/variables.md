# Variables

## Declare variables

Marcel variable declarations are the same as Java's

````java
int a = 2
Object o = new Object()

list<int> l = [1, 2] // collection of primivites
````

### Multiple declarations
Marcel supports multiple assignments in one statement

```java
def (int a, String b, Object c) = [1, "2", new Object()]

def (int d, String e, Object f) = functionReturningAnArrayOrList()
```

Note that if the array/list is shorter than the number of variable declared, this will lead to a runtime error

<br/>
Sometimes you might want to ignore a specific item of a list,
You can use the `_` identifier to let the compiler know that.

E.g.

```java
def (_, String world) = ("hello world" =~ /hello (world)/).groups()
```

## Variable assignments

Just use `=` to assign values to defined variables

```java
int a = 2

a = 3
```


### Automatic casting
Variable assignments are automatically casted when needed.

```java
Optional o = Optional.of(123)
Integer myInteger = o.get() // returns Object cast into an Integer 
int myInt = o.get() // returns Object cast into an Integer then into an int
```

This can be useful as Marcel [doesn't support generic types](./types.md#generic-types).