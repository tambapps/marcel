# Lambdas

Marcel supports lambda expressions, but note that they are not compiled to Java lambdas, they are compiled to anonymous classes instead.


Lambda are declared like Kotlin's, and Groovy closures. They can be used for any functional interfaces, such as Runnable, Supplier...

## Lambdas with no parameters


```marcel
Lambda0 l0 = {
  
}
```
## Lambdas with 1 parameter

If you don't specify any parameter, an implicit `Object it` parameter will be declared.
```marcel
Lambda0 l1 = {
  println(it)
}
```

Or you can explicitly declare it yourself, specifying the type
```marcel
Lambda1 l1 = { Integer p0 ->
  println(p0)
}
```
## Lambdas with 2+ parameter
You have to declare explicitly all parameters, separated by a comma

```marcel
Lambda3 l3 = { Integer foo, Long bar, String zoo ->
  // do something
}
```