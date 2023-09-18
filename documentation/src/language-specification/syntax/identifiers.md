# Identifiers

An identifier is a name that you can use to define a class, field, a function or variable.

An identifier must follows all the below rules

- it must start with a letter ('a' to 'z' , and 'A' to 'Z'), or an underscore
- the following characters can be a letter, an underscore or a number

## Class identifiers

To reference a class, you need tp add the `.class` suffix, like in Java.
But note that you can only reference simple name of classes, this means that you need to import it first.

```marcel
import java.util.concurrent.Callable
println(Callable.class)

println(Object.class)
```