# Classes

You can define classes using the `class` keyword

```kotlin
class Foo {

}
```

## Extending/Implementing classes/interfaces

The syntax is like Java's

```java
class Foo extends Object implements List<Integer> {

}
```

## Class visibility

You can specify your class's [visibility](./visibility.md) before the `class` keyword

```kotlin
public class Foo {

}
```

## Class functions

See the [functions section](./functions.md) to see how to define functions

## Class fields

You can define class fields like you would in Java

```java
class Foo {
  private int a;
  double b = 3
  Object c;
}
```

## Constructors
You can use the keyword `constructor` to define constructors. The definition is similar to a function

```kotlin
class Foo {
  int bar
  String zoo
  
  constructor(int bar, String zoo) {
    this.bar = bar
    this.zoo = zoo
  }
}
```

Constructors where you just want to assign values to your fields are common use-cases. Marcel has a syntax
allowing you to write such constructors with a less verbose code.
```kotlin
class Foo {
  int bar
  String zoo
  
  constructor(this.bar, this.zoo)
}
```

We didn't even specify a function block, but you can specify one if you want. The first statements
of your class will be the field assignments (after the super() call of course).

### Calling constructors
You can call specific `super` and `this` constructors.

```groovy
class A {
  int foo
  constructor(this.foo)
}

class B {
  int bar
  constructor(int foo, this.bar): super(foo) {
    println("Yahoo")  
  }
  
  constructor(this.bar): this(0, bar)

}
```
