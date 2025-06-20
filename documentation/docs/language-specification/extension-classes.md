# Extension classes

Extension classes allows you to add methods to an existing class.

## How to declare
An extension class is declared like a regular class, but with the keyword `extension`. You'll
also need to specify which class your are extending.
```marcel
extension class MyExtension for Integer {
}
```
## Define instance methods

Define methods as you would if you were in the class you are extending (not talking about inheritance).

```marcel
extension class MyExtension for Integer {
  
  fun int next() {
    return this + 1
  }

  fun float foo() {
    return floatValue() + 2f * next()
  }

}
```

As you can see in the above example, you can also call methods of the extended class, and other extensions methods 
you defined.

## Define static methods
This works the same as instance methods. Define your static method as if you were in the extended class.
```marcel
extension class MyExtension for Integer {
  
  static fun int zoo() {
    return 1
  }

}
```
 
## How to use

Import your extension class with the `extension` keyword.


```marcel
// another file
import extension MyExtension

Integer a = 1
println(a.next())
println(Integer.zoo())
```

Note that you can also use an extension in the same file it was declared in. In such case, you don't
need to specify the import

```marcel
extension class MyExtension for Integer {
   fun int next() {
      return this + 1
    }

    fun float foo() {
      return floatValue() + 2f * next()
    }

   static fun int zoo() {
      return 1
    }
}

Integer a = 1
println(a.next())
println(Integer.zoo())
```