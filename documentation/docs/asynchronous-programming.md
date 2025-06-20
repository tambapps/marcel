#  Asynchronous programming in Marcel
This feature is currently under development and is therefore not available yet.

Marcel provides an async/await paradigm allowing to execute tasks in the backgrounds.
In Marcel, asynchronous (AKA `async`) code will be executed on background threads (virtual threads if you JRE supports it).

## Usage

### Async functions
Async functions provide a way to write functions that are executed in the background when called
```marcel
async fun int compute() {
  Thread.sleep(2000l)
  return 1
}
```
The actual return type of async functions are [Futures](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Future.html)
(in above example a `Future<Integer>`).

### Async block
An `async` code block is a block in which you can perform async operations
```marcel
async fun int doCompute() -> 1

async {
  int result = await(doCompute())
  result
}
```

`async` blocks will always wait (at the end) for all asynchronous tasks to complete.
Although this is done automatically, you can also do it manually with the `await` function.

### Async context
It's important to know that **async functions can only be executed in an async context.**, that is in an async function or in an async block (we'll get on that later).


### Await
The `await` keyword allows to wait for the result of an asynchronous function.
```marcel
async fun int computeInBackground() {
  int result = await doCompute()
  println(result)
}
```

There are many ways to await an asynchronous process.

#### await

Await is (a set of) static methods that you can use in `async` contexts.

#### await()
Awaits for all asynchronous tasks to complete.

#### await(Future)
Awaits for a particular asynchronous task to complete.

#### await(Collection), await(Object[])
Awaits for a collection/array of asynchronous tasks to complete.


#### await(AwaitProgressListener)
Awaits using the provider lambda to listen to progress update.
E.g.
```marcel
async {
  doCompute()
  await { int completedTasks, int total -> 
    print("\rComputed $completedTasks out of $total configurations")
  }
}
```

E.g. the below code example wouldn't compile
```marcel
async fun int doCompute() -> 1

fun void computeInBackground() {
  int result = await doCompute() // Compiler error: cannot call async function in a non async context
  println(result)
}
```

## Under the Hood of Async Programming

The library implementing asynchronous programming in Marcel is [Threadmill](https://github.com/tambapps/marcel/tree/main/subprojects/threadmill/README.md), a subproject of Marcel.

### How async processes are executed
Calling an asynchronous function will actually supply a new Callable to an executor and return the resulting [Future](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Future.html).

Executing an `async` block will actually supply a new `Runnable` to the executor in order to execute the block in the background.
