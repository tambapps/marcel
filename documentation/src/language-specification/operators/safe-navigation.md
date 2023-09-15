# Safe Navigation Operator (?.)

The safe navigation operator is used to access a property of an object that might be null, without getting a `NullPointerException`.

It is a simple syntax allowing you to simple code. In Java, you could code

```java
Foo foo = getFoo()
Bar bar = foo != null ? foo.getBar() : null
```

In Marcel, you would code

```java
Foo foo = getFoo()
Bar bar = foo?.bar // Marcel recognizes getters and translates '.bar' into '.getBar()' at compilation
```