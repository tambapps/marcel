# Program structure

## Package
A Marcel source file can have a package. It is optional but if it is specified, it must be the first instruction in the file
(excluding comments)

``java
package my.package
``

## Imports
Then, some imports can follow. You can consult the default imported class/package [here](./imports.md#default-imports)


## Scripts
If you intend to write a script, there is no need to define a main() function. You can just start writing statements of your script.

You can also define functions in scripts

## Class

You can define classes like in Java. 

Note that if you define a class in a script, it will be an inner class (its outer class being the script's class)
