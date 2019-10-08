# Getting started with KIO

KIO is a side effect wrapper that suspends side effects in order to provide referential transparency even when
your code need to interact with the outside world. The `KIO` data type has three type parameters:
 - `R`: represents the type you are going to inject in the environment in order to provide the needed dependencies, 
 configurations or parameters.
 - `E`: represents the data type that is used when the monad is in the "failure" state.
 - `A`: represents the data type that is provided if all the processing is successful.

So, basically, you can see `KIO<R, E, A>` as a wrapper to:
```kotlin
(R) -> Either<E, A>
``` 

In order to manage in a simpler way the instances, some useful type aliases are provided:

```kotlin
typealias IO<E, A> = KIO<Any, E, A>
typealias URIO<R, A> = KIO<R, Nothing, A>
typealias UIO<A> = URIO<Any, A>
typealias Task<A> = IO<Throwable, A>
typealias RIO<R, A> = KIO<R, Throwable, A>
```

that basically means:

 - `IO`: an instance of KIO where there is no R injected;
 - `UIO`: a computation that can’t fail (or at least that isn’t typed for failure);
 - `URIO`: like UIO, but with the injection of type R;
 - `Task`: an instance of KIO without R injection and where the error type is set to Throwable;
 - `RIO`: like Task, but with R injection;

you can also create your own combination even if there is no a type alias already provided by the library.

###How to create a KIO instance

In order to create a KIO instance, some functions are provided:
 - `just`: returns an instance of KIO that wraps an already computed value; nothing is deferred in this case.
 - `failure`: it is basically the same of `just`, but the provided value is used to create a KIO in failure state.
 - `effect`: creates an instance of KIO that suspends the provided lambda function. This is useful in order to wrap
 side effects that you don't expect to fail (e.g. random number generation).
 - `unsafe`: creates an instance of KIO that suspends the provided lambda function. The difference with `effect` is
 that the `E` type is defined as `Throwable` as we expect that the provided code could fail throwing an exception.
 - `suspended`: like `effect`, but the provided lambda is defined as a suspend function.
 - `unsafeSuspended`: like `unsafe`, but the provided lambda is defined as a suspend function.

Some additional notes:
 - `suspended` and `unsafeSuspended` can be used only when the `RuntimeSuspended` is used (more later).
 - also the functions `justR`, `failureR`, `effectR`, `unsafeR`, `suspendedR` and `unsafeSuspendedR` are provided;
their behaviour is exactly the same of the non-R versions. The only difference is in how the `R` type in handled
in order to help the type inference when using the `R` type for injection.

### Accessing the environment

If you are going to use the `R` parameter, you can retrieve the injected data using the `ask` function if you are going 
to instantly wrap a side effect with the provided data, or the `askPure` function where you optionally can provide
a mapping function.

### Concurrency

KIO doesn't provide advanced tools for concurrency management. There are only two functions that you can use for this
purpose, that are:
 - `parMapN`: executes the provided KIO instances in parallel and, once all the instances are concluded, in provides
 the results to the mapping function.
 - `race`: executes the provided KIO concurrently and, when the fastest of is done, all the other are cancelled and
 the result of the faster one is returned.
 
Note: in order to use these functions you need to use the `RuntimeSuspended` version of the runtime.  

### Executing the program

KIO provides two runtime for program execution: `Runtime` and `RuntimeSuspended`. The first one doesn't support
coroutines, suspend functions and therefore the concurrency primitives; RuntimeSuspended, on the other hand, supports
all the coroutines goodies but, if you are not already inside a suspend function, it add the overhead of the creation
of a new coroutine context in order to execute the code.

When executing the code, you can provide the `R` instance you are going to inject. Also, for the suspended version,
you can provide the `CoroutineContext` you want to use for the execution.

### Optional values

KIO can be used also for modelling an optional value, by simply using the `Empty` object as type and value for `E`.
Be warned that this is suitable only when you are going to manage deferred computations that could return a value or not.
Instead, if you need to describe a data type that can have a value or not inside your data structures, the philosophy
of KIO is to don't use the Optional data type but instead just use nullable types, that in Kotlin are safe to use,
unlike Java. Also, the KIO library provides some utility functions in order to easily manage them:
 - `then`: you can see this exactly as a `map` function. The name is different just because of naming collisions.
 - `orElse`: just an alternative and more readable way to use the elivis operator.  
