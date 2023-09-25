# marcel
Marcel is language that compiles to Java bytecode, with a guaranteed support for Android. It also aims to use primitive types as much as possible when
working with Collections.

You can learn more about this programming language on [its website](https://tambapps.github.io/marcel)
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
