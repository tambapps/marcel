# String

The Java String as you know it.  There are different ways to create strings in Marcel

### Simple strings
You can use the single quote character (`'`) to create strings
```marcel
'Hello world!'
```


### Interpolated strings

You can use the double quote character (`"`) to create strings resolving variables

```marcel
"$firstName $lastName is $age years old"
```

If you need to access a property, use the brackets


```marcel
"${person.firstName} ${person.lastName} is ${person.age} years old"
```


### Pattern strings

You can instantiate [Patterns](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html) using backslash strings.
These strings are reserved for pattern only.


The backslash is **not** considered as an escape, except for the backlash character
(which would be escaped as `\/`).

```marcel
r/some \w+/
```

Note that such strings **doesn't** resolve variables. If you want to construct a Pattern while resolving Strings, you could
just call the `Pattern.compile(String)` method with an interpolated string.

<br/>

It is good practise to end such regexes with a semi-colon (`;`) character, to make it clear to the compiler that what follows
is not a regex flag (we'll talk about that just after) but a 'real' identifier.

E.g.

```marcel
Pattern pattern = r/myPattern/; // without the semi-colon, Marcel would think that 'println' characters are regex flags
println(pattern)
```


#### Pattern flags

You can also specify flags by adding a suffix at the end of your regex String.


```marcel
Pattern pattern = r/myPattern/iu; // you can specify many flags at once
println(pattern)
```

Here is the list of flags (you can see the doc of each flag in the [Javadoc of the Pattern's class](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#UNIX_LINES)).


| character | Java PatternFlag        |
|-----------|-------------------------|
| d         | UNIX_LINES              |
| i         | CASE_INSENSITIVE        |
| x         | COMMENTS                |
| m         | MULTILINE               |
| l         | LITERAL                 |
| s         | DOTALL                  |
| u         | UNICODE_CASE            |
| c         | CANON_EQ                |
| U         | UNICODE_CHARACTER_CLASS |
