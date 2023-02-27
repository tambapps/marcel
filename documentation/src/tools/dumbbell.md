# Dumbbell - Marcel's dependency manager


Dumbbell is a dependency manager for Marcel. It allows you to easily import dependencies (from Maven central) in your scripts,
without having to create a Maven/Gradle project.

It was inspired from [Groovy's grapes](https://groovy-lang.org/grape.html)

<br/>

Dumbbell doesn't exist yet, but I look forward to develop and release it.


To import a dependency, use the `dumbbell` keyword.

```groovy
dumbbell 'com.google.code.gson:gson:2.10.1'
import com.google.gson.Gson

Gson gson = new com.google.gson.Gson()

println(gson.toJson(['a': 'b']))
```



Dumbbell is also used in [Marshell](./marshell.md). Use the `:pull` command to pull the 
dependency dynamically.