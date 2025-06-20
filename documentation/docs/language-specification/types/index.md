# Types

In this section we'll explore some common Marcel types.

## Java Types

Marcel is a JVM language, therefore you can use any classes defined in the JDK.


## Generic Types

Marcel does **not** support generic types (except for collections of primitives which technically aren't generic as they are mapped to specific interfaces). 

You can use generic classes but cannot specify type parameters when using them.
It's a conscious choice made to get rid of some complexity while developing the compiler and also because Java always casts at runtime anyway.

