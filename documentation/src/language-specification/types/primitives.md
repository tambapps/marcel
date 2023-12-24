# Primitives
Marcel supports the following Java primitives

- void
- boolean (bool)
- byte
- short
- int
- long
- float
- double
- char
- byte

## Literal Numbers

Marcel supports almost all Java primitives. The number primitive literals are the same as in Java

```marcel
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

```marcel
int i = 0b10
long l = 0b11l
```


### Hexadecimal representation

You can also create numbers using their hexadecimal representation with the `0x` prefix

```marcel
int i = 0x5
long l = 0x5l
```

## boolean

You can create booleans using the `true` or `false` keyword.

```marcel
bool b = true
```

### char
Use the backtick (<code>`</code>) to create primitive characters.
Only one character must be specified between the two backticks


```marcel
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

