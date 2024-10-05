# Dynamic Objects

Dynamic Objects are the one and only dynamic feature in Marcel. They allow you to manipulate any kind of objects using 
dynamic properties and method calls.

## What does it do
[DynamicObject](https://github.com/tambapps/marcel/blob/main/marcel-stdlib/src/main/java/marcel/lang/DynamicObject.java) (or `dynobj`)
is an interface that is handled specially by the Marcel compiler. All field access, method calls and operator uses on a DynamicObject 
are resolved at runtime instead of compile-time.

The DynamicObject wraps an actual (and non dynamic) object. Various types are handled in order to make them easy to manipulate through the 
DynamicObject API. For example, you can manipulate maps like objects with properties.

```marcel
dynobj dMap = [foo: 'bar', zoo: 'pew'] as dynobj

println(dMap.foo)
dMap.zoo = 8
```


Note that dynamic method calls won't be applicable for all methods of the actual object wrapped by the dynobj, this feature is limited.
And if you attempt to call a method that isn't defined/handled, you will get an error **at runtime**.

```marcel
dynobj o = 1
println(o[1]) // will throw MissingMethodException at runtime, instead of a semantic error at compile time
```
The same behaviour applies for field access.

## Register fields/methods
Dynamic Objects allow you to register method/fields to specific instances. Use the `registerMethod`/`registerField` methods for that.

```marcel
dynobj o = 1

o.registerMethod("foo", Integer.class) { Integer i -> i * 2 + 1 }
println(o.foo(1)) // 3

o.registerField("bar", "value")
println(o.bar) // value
o.bar = "new value"
println(o.bar) // new value
```