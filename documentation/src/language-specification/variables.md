# Variables

## Declare variables

Marcel variable declarations are the same as Java's

````groovy
int a = 2
Object o = new Object()

list<int> l = [1, 2] // collection of primivites
````

### Multiple declarations
Marcel supports multiple assignements in one statement

```groovy
def (int a, String b, Object c) = [1, "2", new Object()]

def (int d, String e, Object f) = functionReturningAnArray()

```

Note that if the array is shorter than the number of variable declared, this will lead to a runtime error

## Variable assignments

Just use `=` to assign values to defined variables

```java
int a = 2

a = 3
```