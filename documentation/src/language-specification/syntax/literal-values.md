## Strings

There are different ways to create strings in Marcel

### Simple strings (not yet implemented)
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


### Character
Use the character backtick (<code>`</code>) to create characters.
Only one character must be specified between the two backticks


```
`A`
```



### Escaped characters
Use backslash to escape 'special' characters. Here is the list of escaped characters

| escaped character | represented value                               | 
|-------------------|-------------------------------------------------|
| \b                | backspace	                                      | 
| \n                | newline	                                        | 
| \r                | carriage return	                                | 
| \t                | tabulation	                                     | 
| \\                | backslash	                                      | 
| \'                | single quotes (useful in simple strings)	       | 
| \"                | double quotes (useful in interpolated strings)	 | 


## Literal Numbers

Marcel supports almost all Java primitives. The number primitive literals are the same as in Java

```java
// primitive types
byte  b = 1
char  c = 2
short s = 3
int   i = 4
long  l = 5
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

[comment]: <> (TODO need to add link)
Note that you can also use this syntax to create collections.

## Maps

Square brackets can also be used to define maps

````groovy
Map map = [1.3: "1", 1.4: "2", "myStringKey": "myStringValue", myRefKey: myRefValue]
````