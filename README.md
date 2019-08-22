# defexception
[![Clojars Project](https://img.shields.io/clojars/v/defexception.svg)](https://clojars.org/defexception)

A simple Clojure library that allows one to dynamically define Java Exception classes in Clojure.

```clj
[defexception "0.0.1-SNAPSHOT"]
```

## Motivation

Sometimes you just want a quick Exception class that you can catch
directly. `defexception` uses `clojure.asm` to dynamically create a
Java class that directly inherits from `clojure.lang.ExceptionInfo`.

## Usage

To create your own exception class you can do this:

```clojure
(ns foo.bar
 (:require '[defexception.core :refer [defexception]]))

(defexception MyException)
```

This will create the `foo.bar.MyException` class that inherits from
`clojure.lang.ExceptionInfo`. This will also `import` the class into
the current namespace and create a helper function
`foo.bar/->MyException` to help you construct the exception class.

Now you can do this:

```clojure
(try
  (throw (->MyException "My bad!" {:my-bad 1}))
  (catch MyException e
    (ex-data e)))
;; => {:my-bad 1}
```

The above expression will return `{:my-bad 1}`.

The generated `->MyException` helper function has several signatures
to help you instantiate your exception.

```clojure
;; creates an exception with no message or ex-data
(->MyException) 

;; creates an exception with only ex-data
(->MyException {:hello 1}) 

;; creates an exception with only a message
(->MyException "My Bad!") 

;; creates an exception with both a message and ex-data
(->MyException "My Bad!" {:hello 1}) 

;; creates an exception with a message, ex-data and a cause
(->MyException "My Bad!" {:hello 1} (Exception. "The cause"))
```

## AOT compilation

Care was taken to make these exceptions compatible with Clojure's AOT
compilation.

## License

Copyright © 2019 Red Planet Labs Inc.

Distributed under the Eclipse Public License version 1.0