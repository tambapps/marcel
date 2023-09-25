# Maps


Square brackets can also be used to define maps

````marcel
Map map = [1.3: "1", 1.4: "2", "myStringKey": "myStringValue", 
           myLiteralKey: myRefValue, (myRefKey): myRefValue]
````

Note that `myLiteralKey` is actually a String key, it doesn't refers to a variable (like in Groovy). If you want to reference
a variable as a key, put it between parenthesis, like it is done for `(myRefKey)`.
