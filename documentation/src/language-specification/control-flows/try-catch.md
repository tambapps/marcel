# Try/Catch/Finally (Not Yet Implemented)

Exception handling is very similar as Java's.


````groovy
try {
  Object a = null
  println(a.hashCode())
  println("Successfully tried")
} catch (IOException|NullPointerException e) {
    println("Caught exception")
} finally {
  println("finally")
}
````

The above code will print
`````text
Caught exception
Finally
`````

## Try with resources (Not Yet Implemented)

TODO