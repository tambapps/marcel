# Meta-annotations (metaprogramming with annotations)
[AST transformations](index.md) can be specified on annotations. Doing so allows to perform specific transformations when annotating a given
class, field, method and/or field.

Marcel's standard library provides many annotations useful to avoid writing boilerplate code. Some of them are similar as the one you could find in [Lombok](https://projectlombok.org/).

Meta-annotations from the Marcel's standard-library are all lowercase (even the first letter), this is how you can differentiate them from
other (non-meta) annotations.

## @cached

This annotation allows to cache results of a method in order to prevent having to compute multiple times the result for a same input.
It will generate a cache and make the annotated method use this cache. The cache key is produced with the method parameter(s).

If the value for the given input (method parameters) is in the cache, we just return it from the cache. If it isn't in it, we compute the value, put
it in the cache and then return it.


A perfect example is a recursive implementation of the fibonacci suite. With high numbers, we may compute multiple times the value
for a same input, making the operation really long. That's where `@cached` comes to save the day. No value for a same input will be computed more than once, this
will allow to save a lot of time.

```marcel
println(fibonacci(10))

@cached
fun int fibonacci(int n) -> switch (n) {
  0, 1 -> n
  else -> fibonacci(n - 1) + fibonacci(n - 2)
}
```

This script would print `Foo(bar=myBar)`.

Caching also work with functions having many parameters

```marcel
println(funnynacci(10, 15))

@cached
fun int funnynacci(int n, int m) -> switch (n) {
  0, 1 -> n + m
  else -> funnynacci(n - 1, m - 1) + funnynacci(n - 2, m - 1)
}
```
### Thread-safe cache
By default, the cache is **not** thread safe and is backed by a [HashMap](https://docs.oracle.com/javase/8/docs/api/java/util/HashMap.html).

You can set the `threadSafe` flag to `true` if you want to make the implementation thread-safe. The cache will then be backed by
a [ConcurrentHashMap](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentHashMap.html).

```marcel
println(fibonacci(10))

@cached(threadSafe=true)
fun int fibonacci(int n) -> switch (n) {
  0, 1 -> n
  else -> fibonacci(n - 1) + fibonacci(n - 2)
}
```

## @comparable

This makes your class implement [Comparable](https://docs.oracle.com/javase/8/docs/api/java/lang/Comparable.html) interface. The comparison will be made based on your class's field, in the order
in which they were defined.

For example take a look at the below class

```marcel
@comparable
class Foo {
  int a
  String b
  double c
}
```

When comparing 2 Foo instances, we'll start by comparing the fields `a`. If they are not the same (one is greater/lower than the other),
we'll stop the comparison here as we already can determine which Foo instance is greater than the other. If both `a` fields hold the same value,
we'll continue the comparison with `b`, and so on and so on...

### Include getters
The getters are not included in the generated Comparison by default. To change this behaviour, you can use the flag `includeGetters=true`.

#### Exclude particular fields/methods
You can use the annotation `@comparable.Exclude` to exclude a particular field or getter.

```marcel
@comparable(includeGetters=true)
class Foo {
 int i = 1

 @comparable.Exclude
 String b = "srsr"

 @comparable.Exclude
 fun String getFoo() -> "foo"

 fun String getBar() -> "bar"
}
```

## @data

This annotation is similar to [Lombok's @Data annotation](https://projectlombok.org/features/Data).
It auto-generates the `equals()`, `hashCode()` and `toString()` method for your class, based on your class's members.

The

You can also make your class [implement Comparable](#comparable) by providing the `comparable=true` flag

```marcel
@data
class Foo {
  int a = 2
  String b = "b"
}
```

#### Exclude particular fields/methods
You can use the `@data.Exclude`, `@comparable.Exclude` or `@stringify.Exclude` annotations to exclude properties from the sring representation,

`@data.Exclude` will exclude the property from both the `toString()`, and the `equals()`,`hashCode()` methods.
`@comparable.Exclude` will only exclude the property for the `equals()`,`hashCode()` methods.
`@stringify.Exclude` will only exclude the property for the `toString()` method.

```marcel
@data
class Foo {
 @data.Exclude
 int i = 1

 @comparable.Exclude
 String b = "srsr"

 @comparable.Exclude
 fun String getFoo() -> "foo"

 fun String getBar() -> "bar"
}
```

## @lazy

This annotation is used to make a field lazy. The value of the field will only be computed when it is referenced, and
not before.

### Prerequisites

The annotated field must be of Object type (non-primitive) and must have an initial value specified.

E.g.

```marcel
@lazy
Integer i = computeI()

fun int computeI() {
  initCount++
  return 1
}
```

### How it is transformed

This meta-annotation will transform the code to make the annotated field lazy.

Using the above example, the code would be transformed as such

```marcel
private Integer _i = null

fun getI() {
  if (_i == null) {
    _i = computeI()
  }
  return _i
}

fun int computeI() {
  initCount++
  return 1
}
```
As Marcel allows to [access getters as properties](../language-specification/variables.md#properties), you can access this variable using
the `a` syntax (or `this.a`), as if you were referencing the original field.
You'll notice that in the above script, the value of `i` will never be computed, as the variable `i` wasn't referenced anywhere outside.

Now let's take a look at a full example.

```marcel
@lazy
Integer i = computeI()

for (int _ in 1..3) println(i)

fun int computeI() {
  initCount++
  return 1
}
```

The output of the above script would be

```text
Computing...
1
1
1
```

As the variable is lazy, the value of i will be computed at the first call of `println(i)`, and the other calls
will just use the already computed value.


## @stringify

This annotation is similar to [Lombok's @ToString annotation](https://projectlombok.org/features/ToString).
It auto-generates a `toString()` method for your class, based on your class's members.

The generated toString method will put all fields of the class with their values in the generated String.

```marcel
@stringify
class Foo {
 String bar = "myBar"
}

println(new Foo())
```

This script would print `Foo(bar=myBar)`.

### Include getters
The getters are not included in the generated String by default. To change this behaviour, you can use the flag `includeGetters=true`.

```marcel
@stringify(includeGetters=true)
class Foo {
 String bar = "myBar"
 
 fun int getZoo() -> 5 
}

println(new Foo())
```

This script would print `Foo(bar=myBar, zoo=5)`.

#### Exclude particular fields/methods
You can use the annotation `@stringify.Exclude` to exclude a particular field or getter.

```marcel
@stringify(includeGetters=true)
class Foo {
 int i = 1

 @stringify.Exclude
 String b = "srsr"

 @stringify.Exclude
 fun String getFoo() -> "foo"

 fun String getBar() -> "bar"
}
```

This script would print `A(i=1, bar=bar)`. 
