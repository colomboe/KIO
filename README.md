# KIO [![Build Status](https://travis-ci.org/colomboe/KIO.svg?branch=master)](https://travis-ci.org/colomboe/KIO)
A simple, lightweight IO monad implementation for Kotlin inspired to the effect-rotation based ZIO library for Scala.

### Introduction
Many functional programming languages (like Clojure and F#), even if they could (more or less) support the 
Haskell typeclasses-like FP approach, doesn't rely on it in order to provide a
functional programming environment.
Instead, they just rely on the most important pillars of functional programming that are 
__referential transparency__ and __immutability__, usually complemented with the use 
of __algebraic data types__ (ADTs).
 
 Kotlin already provides many tools that can help the developers to stick with these principles, like _data classes_,
 _sealed classes_ and _immutable variables_ (`val`).
 The two most notable missing pieces are a way to easily handle referential transparency 
 when side effects are involved and an error handling alternative to exceptions.
 KIO is a lightweight IO implementation that tries to fill this gap without aspiring to provide all the classic
 Haskell/Scala typeclass categories. If you are looking for this type of library, 
 take a look at [Arrow](https://arrow-kt.io) instead.

### Features
KIO provides a IO monad-like implementation specific for Kotlin with the following features:
 - Small and lightweight;
 - Everything is lazy (deferred);
 - Support for typed errors (like the Either construct);
 - Support for exception handling (like the Try construct);
 - Support for optional values (like the Option construct);
 - Support for injecting an _environment_ (data or functions) to the program (like the ReaderT construct); 
 - Support for asynchronous and parallel processing via native __coroutines__ integration;
 - Support for atomic, concurrent and mutable references.
 
 Also some utility methods are available for lists and null-handling.
 
