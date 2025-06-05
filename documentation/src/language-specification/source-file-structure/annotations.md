# Annotations

Annotations are a lot like Java's. Use the `@MyAnnotation` syntax to annotate a class, field or method parameter.

```marcel
@MyAnnotation
class MyClass {
  @MyAnnotation
  int myField

  fun int myMethod(@MyAnnotation Integer someInt) {
    return someInt + myField
  }
}
```

## Annotation attributes

You can specify attributes like in Java. If your annotation only has one attribute, with the name `value()` You can just 
type your attribute value between parentheses.

```marcel
@MyAnnotation(1)
```

Or you can specify the attribute name like in the below example

```marcel
@MyAnnotation(value = 1)
```

If you have multiple attributes, separate them with a comma

```marcel
@MyAnnotation(value1 = 1, value2 = 3)
```

### Enum attributes
When specifying enum attributes you just have to specify the enum's name, without its class

```marcel
@MyAnnotation(timeUnit = MILLISECONDS)
```

### Class attributes

For attributes of type `Class`, you can specify any class as you would in Java

```marcel
@MyAnnotation(converter = MyConverter.class)
```

But like in Groovy, you can also specify Lambdas

```marcel
@MyAnnotation(converter = { it.toString() })
```
A lambda generates a class at compilation. This generated class will be used as the value for this attribute.