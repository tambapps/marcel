# Types

In this section we'll explore some common Marcel types.

## Java Types

Marcel is a JVM language, therefore you can use any classes defined in the JDK.


## Generic Types

Marcel **doesn't** support generic types except for collections of primitive (which technically aren't really generic). You can use generic classes but cannot specify generic types when using them.
it's a conscious choice made to get rid of some complexity while developing the compiler and also because Java always casts at runtime anyway.

