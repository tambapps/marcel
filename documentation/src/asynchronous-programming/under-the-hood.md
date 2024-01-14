# Under the Hood of Async Programming

The library implementing asynchronous programming in Marcel is [Threadmill](https://github.com/tambapps/marcel/tree/main/subprojects/threadmill/README.md), a subproject of Marcel.

## How async processes are executed
Calling an asynchronous function will actually supply a new Callable to an executor and return the resulting [Future](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Future.html).

Executing an `async` block will actually supply a new `Runnable` to the executor in order to execute the block in the background.
