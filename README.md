# Marcel
Marcel is language that compiles to Java bytecode, with a guaranteed support for Android. It also aims to use primitive types as much as possible when
working with Collections.

You can learn more about this programming language on [its website](https://tambapps.github.io/marcel)

## Rewrite
Marcel is undergoing a big refactoring of the parsing, semantic analysis and compiler. Building it from source might not work
at the moment but you can still [install it from a release](https://tambapps.github.io/marcel/getting-started/installation.html#install-from-release)
## Example

Here is an example on how to code the Fibonacci suite in Marcel

```kotlin
println(fibonacci(10))

fun int fibonacci(int n) {
  return switch (n) {
    0, 1 -> n
    else -> fibonacci(n - 1) + fibonacci(n - 2)
  }
}
```

## Install it

### From source
You can run the `install.sh` to install Marcel on your computer. It will build the jars using maven and then put them in a directory.
You have to be located on this project's root directory when executing the script.

```shell
./install/install-from-source.sh
```

### From release


## Maven Plugin

You can find in this repository the official Maven Plugin to compile Maven projects with
Marcel source code
