## Strings

There are different ways to create strings in Marcel

### Strings
You can use the single quote character (`'`) to create strings
```kotlin
'Hello world!'
```


### Interpolated strings

You can use the double quote character (`"`) to create strings resolving variables

```kotlin
"$firstName $lastName is $age years old"
```

If you need to access a property, use the brackets


```kotlin
"${person.firstName} ${person.lastName} is ${person.age} years old"
```


### Pattern strings

You can instantiate [Patterns](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html) using backslash strings. 
These strings are reserved for pattern only.


The backslash is **not** considered as an escape, except for the backlash character
(which would be escaped as `\/`).

```javascript
/some \w+/
```

Note that such strings **doesn't** resolve variables. If you want to construct a Pattern while resolving Strings, you could
just call the `Pattern.compile(String)` method with an interpolated string.

<br/>
It is good practise to end such regexes with a semi-colon (`;`) character, to make it clear to the compiler that what follows
is not a regex flag (we'll talk about that just after) but a 'real' identifier.

E.g.

```javascript
Pattern pattern = /myPattern/; // without the semi-colon, Marcel would think that 'println' characters are regex flags
println(pattern)
```


#### Pattern flags

You can also specify flags by adding a suffix at the end of your regex String.


```javascript
Pattern pattern = /myPattern/iu; // you can specify many flags at once
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


### Character
Use the backtick (<code>`</code>) to create primitive characters.
Only one character must be specified between the two backticks


```java
char c = `A`
```


### Escaped characters
Use backslash to escape 'special' characters within strings/characters. Here is the list of escaped characters

| escaped character | represented value                              | 
|-------------------|------------------------------------------------|
| \b                | backspace	                                     | 
| \n                | newline	                                       | 
| \r                | carriage return	                               | 
| \t                | tabulation	                                    | 
| \\\\              | backslash	                                     | 
| \\\'              | single quotes (useful in simple strings)	      | 
| \\\"              | double quotes (useful in interpolated strings) | 
| \\\`              | backtick (useful in character strings)	        | 


## Literal Numbers

Marcel supports almost all Java primitives. The number primitive literals are the same as in Java

```java
// primitive types
byte  b = 1
short s = 2
int   i = 3
long  l = 4l
float f = 5f
double d = 6d
```

### Binary representation

You can also create numbers using their binary representation with the `0b` prefix

```java
int i = 0b10
long l = 0b11l
```


### Hexadecimal representation

You can also create numbers using their hexadecimal representation with the `0x` prefix

```java
int i = 0x5
long l = 0x5l
```

## Literal Booleans

You can create booleans using the `true` or `false` keyword.

```java
bool b = true
```

## Arrays


Create arrays with the square brackets

```groovy
int[] ints = [1, 2, 3, 4]
```

Note that you can also use this syntax to [create collections](../types.md#collections-of-primitives).

## Maps

Square brackets can also be used to define maps

````groovy
Map map = [1.3: "1", 1.4: "2", "myStringKey": "myStringValue", myRefKey: myRefValue]
````

## Ranges

You can create int (and soon long) ranges

```groovy
0..10 // 0 (inclusive) to 10 (inclusive)
0<..10 // 0 (exclusive) to 10 (inclusive)
0..<10 // 0 (inclusive) to 10 (exclusive)
0<..<10 // 0 (exclusive) to 10 (exclusive)
```

Ranges also work in reverse order

```groovy
10..0 // 10 (inclusive) to 0 (inclusive)
10>..0 // 10 (exclusive) to 0 (inclusive)
10..>0 // 10 (inclusive) to 0 (exclusive)
10>..>0 // 10 (exclusive) to 0 (exclusive)
```

Ranges work with all kinds of int/long expressions

```groovy
int start = computeStart()
int end = computeEnd()

for (i in start..end) println(i)
```