# Basics

## Identifiers

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
## Comments

You can comment your code in Marcel the same way you would in Java.

Define comments like you would in Java. `// ...` for a single line comment, and `/* ... */` for a multi-line comment


```marcel
// this function does stuff
doStuff()

/* this function
  does some
  other stuff
 */
doOtherStuff()
```

## Keywords

Marcel has the following keywords, which you cannot use as variable/function/fields names

- async
- int
- dynobj
- do
- long
- short
- float
- double
- bool
- byte
- void
- char
- fun
- return
- true
- false
- new
- import
- as
- inline
- static
- for
- in
- if
- else
- null
- break
- continue
- def
- class
- extension
- package
- extends
- implements
- final
- switch
- when
- this
- super
- dumbbell
- try
- catch
- finally
- instanceof
- throw
- throws
- constructor
- public
- protected
- internal
- private
- while