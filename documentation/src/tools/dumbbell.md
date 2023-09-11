# Dumbbell - Marcel's dependency manager


Dumbbell is a dependency manager for Marcel. It allows you to easily import dependencies in your scripts,
without having to create a Maven/Gradle project.

It was inspired strongly from [Groovy's grapes](https://groovy-lang.org/grape.html)

<br/>

## Import dependencies in a script

To import a dependency, use the `dumbbell` keyword.

```java
dumbbell 'com.google.code.gson:gson:2.8.6'
import com.google.gson.Gson

Gson gson = new Gson()

println(gson.toJson(['a': 'b']))
```

Dependencies are pulled from Maven central. The list of repository to pull from will be configurable (someday).


Note that this feature **only works when running scripts with [MarCL](./marcl.md)**.

## Import dependencies in Marshell
Dumbbell is also used in [Marshell](./marshell.md). Use the `:pull` command to pull 
dependencies dynamically.


