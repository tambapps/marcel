# Dynamic Objects

Dynamic objects a little dynamic feature in Marcel. They allow you to evaluate dynamically property/operator calls instead of
at compile time.

E.g.

```groovy
DynamicObject o = DynamicObject.of(1)
println(o[1]) // will throw MissingMethodException at runtime, instead of a semantic error at compile time
```

But the following code will run without throwing any exception
```groovy
DynamicObject o = DynamicObject.of([1, 2, 3] as list<int>)
println(o[1]) // will print 2
```


DynamicObject could potentially handle dynamic method calls and handle properties, but this is **not** done as Marcel
is not designed to be a dynamic language in the first place.