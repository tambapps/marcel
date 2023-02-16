# Program structure

## Package
A Marcel source file can have a package. It is optional but if it is specified, it must be the first instruction in the file
(excluding comments)

## Imports
Then, some imports can follow.

### Class import
Such imports are like Java's

```groovy
import java.text.SimpleDateFormat
```


But Marcel adds the capability to import a class `as` a given name. All references to the given name will be replaced by the
actual class imported when compiling

```groovy
import java.text.SimpleDateFormat as SDF

SDF sdf = someSdf()
```

### Wildcard imports
Again, just like Java
```groovy
import java.text.*
```

### Static functions
Yup, like in Java
```groovy
import static org.junit.jupiter.api.Assertions.assertEquals
```

### Default imports
Marcel import by default all the following packages
- java.lang.*
- java.util.*
- java.io.*
- marcel.lang.*


## Scripts
If you intend to write a script, there is no need to define a main() function. You can just start writing statements of your script.

You can also define functions in scripts

## Classes

You can define classes like in Java. If you define a class in a script, it will be an inner class (its outer class being the script's class)
