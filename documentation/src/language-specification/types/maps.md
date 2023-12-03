# Maps


Square brackets can also be used to define maps

````marcel
Map map = [1.3: "1", 1.4: "2", "myStringKey": "myStringValue", 
           myLiteralKey: myRefValue, (myRefKey): myRefValue]
````

Note that `myLiteralKey` is actually a String key, it doesn't refer to a variable (like in Groovy). If you want to reference
a variable as a key, put it between parenthesis, like it is done for `(myRefKey)`.

## Iterating over maps
To iterate over maps you can use the below syntax

```marcel
Map map = [(1): "some", (2): "another"]
for ((int key, String value) in map) {
  println("$key -> $value")
}
```