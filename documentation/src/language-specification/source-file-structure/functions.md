# Functions

Use the `fun` keyword to define functions

```marcel
fun int sum(int a, int b) {
  return a + b
}

protected fun void foo() {
  // do nothing
}
```

As shown in the above example, a function define has the following structures
1. starts with the [visibility](./visibility.md) which is optional and defaults to `public`.(you can also define static function with the `static` keyword)
2. the `fun` keyword
3. the return type
4. the function's name
5. the list of your function's parameters. The parameter's type first, and then the parameter's name.



## Function Visibility

You can specify your function's [visibility](./visibility.md) before the `fun` keyword

```kotlin
private fun foo() {
}
```

## Function Calls
Function calls are no different than  in any other language

```groovy
int result = sum(1, 2)
```

## Cast Results
Marcel has a diamond operator for function calls which is different from Java's. It casts the 
result of the function to the specified type.

```groovy
Foo otherResult = compute<Foo>()
```
This above example isn't really useful as Marcel [automatically cast variable assignments when needed](../variables.md#automatic-casting) but this feature can be useful when chaining function calls

```groovy
Optional opt = Optional.of(new Foo())
// assuming computeObject() and result return Object in their declaration
Bar result = opt.get<Foo>().computeObject<Bar>()
```
 
But note that this is useless if the function/property already returns the specified type in their declaration.

### Named Parameters Call
You can also specify the name of your parameters. When doing so, the order in which
you specify them doesn't matter.

When you don't specify a parameter, it will default to the type's default value
(0 for primitive types and `null` for Objects).

Following on our `sum()` example:
```groovy
int result = sum(b: 2, a: 1) // equivalent to sum(1, 2)
int otherResult = sum(a: 1) // equivalent to sum(1, 0)
```

named parameters calls can also start (and only start) with positional arguments.

#### Named parameters constructor calls
Such calls also works with constructors. It will find a constructor having the given parameters, but if it can't find it
and your class has a no args constructor (note that there's always one by default if you don't explicitly define one), it will use it
and then initialize the fields having the provided names with their associated argument value.

Here are some examples below.

```groovy
class B {
  int i
  int j
  
  constructor(this.i, this.j)

}
class C {
  int a
  int b
}

B b = new B(i: 1, j: 2) // will call new B(i, j) and the fields will be initialized in the constructor
C c = new C(a: 1, b: 2) // will call new C() and then the fields will be initialized outside the constructor

```

```groovy
int result = sum(2, b: 1) // equivalent to sum(2, 1)


int otherResult = sum(a: 2, 1) // ERROR, positional argument is not at the start 
```

Note that you can only used named parameters call for functions of Marcel-compiled classes, because Java doesn't keep method parameter names available at runtime by default.


## Parameter default value
Function parameters can have default values, which are used when you skip the corresponding argument. These can be useful especially
with named parameters function calls. 

```groovy
fun int sum(int a = 0, int b = 8) {
return a + b
}

sum(a: 2) // 2 + 8
sum(b: 5) // 0 + 8
sum(a: 2, b: 5) // 2 + 5
```

You can specify numbers, characters Strings, or `null`, meaning that Object types can only have `null` as default values
(except String for which you can supply a literal string)

### Fields Constructor Call

Similar calls also work with constructors. You can specify class's field names with their values to set.
Note that it will only work if your class has a no-arg constructor and that the fields referenced are `public` and **not** `final`.


```groovy

Foo foo = new Foo(bar: 1, baz: "baz")

class Foo {
  int bar
  String baz
}
```
