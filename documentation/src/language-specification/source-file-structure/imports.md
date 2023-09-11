## Imports

Marcel's imports are very similar to Java's.

### Class import
Such imports are like Java's

```java
import java.text.SimpleDateFormat
```


But Marcel adds the capability to import a class `as` a given name. All references to the given name will be replaced by the
actual class imported when compiling

```java
import java.text.SimpleDateFormat as SDF

SDF sdf = someSdf()
```

### Wildcard imports
Again, just like Java
```java
import java.text.*
```

### Static imports
Yup, like in Java
```java
import static org.junit.jupiter.api.Assertions.assertEquals
```

### Default imports
Marcel import by default all the following packages
- java.lang.*
- java.util.*
- java.io.*
- marcel.lang.*