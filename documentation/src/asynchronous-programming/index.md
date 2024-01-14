#  Asynchronous programming in Marcel
This feature is currently under development and is therefore not available yet.

Marcel provides an async/await paradigm allowing to execute tasks in the backgrounds.
In Marcel, asynchronous (AKA `async`) code will be executed on background threads (virtual threads if you JRE supports it).

## Async functions
Async functions provide a way to write functions that are executed in the background when called
```marcel
async fun int compute() {
  Thread.sleep(2000l)
  return 1
}
```
The actual return type of async functions are [Futures](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Future.html)
(in above example a `Future<Integer>`).

## Await
The `await` keyword allows to wait for the result of an asynchronous function.
```marcel
async fun int computeInBackground() {
  int result = await doCompute()
  println(result)
}
```

## Async context
It's important to know that **async functions can only be executed in an async context.**, that is in an async function or in an async block (we'll get on that later).

E.g. the below code example wouldn't compile
```marcel
async fun int doCompute() -> 1

fun void computeInBackground() {
  int result = await doCompute() // Compiler error: cannot call async function in a non async context
  println(result)
}
```

### Async block
An `async` code block is a block in which you can perform async operations
```marcel
async fun int doCompute() -> 1

fun void computeInBackground() {
  async {
    int result = await doCompute()
    println(result)
  }
}
```
