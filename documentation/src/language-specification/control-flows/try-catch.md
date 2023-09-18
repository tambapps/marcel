# Try/Catch/Finally

Exception handling is very similar as Java's.


````marcel
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

## Try with resources

Try with resources is like in Java. You can declare Variables as resources and they will be properly closed automatically by the compiler.

````marcel
try (BufferedReader reader = new BufferedReader(new FileReader("/Users/nfonkoua/workspace/marcel/Test.marcel"))) {
  println(reader.readLine())
} catch (IOException e) {
  e.printStackTrace()
}
````