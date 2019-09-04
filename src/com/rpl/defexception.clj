(ns com.rpl.defexception
  (:require [clojure.string :as string]
            [com.rpl.defexception.impl :as impl]))

(defmacro defexception
  "Dynamically creates an clojure.lang.ExceptionInfo class using JVM
  bytecode. The exception class inherits its behavior from
  clojure.lang.ExceptionInfo yet will be its own type.

  This means that an instance of this exception will respond to
  `clojure.core/ex-data` and can have an optional Throwable `cause` arg.

  This will create a constructor function much similar to the one
  created for Records.

  Example Usage:

  (defexception MyException) => user.MyException

  (->MyException)
  (->MyException {:hello 1})
  (->MyException \"This is a message\")
  (->MyException \"This is a message\" {:hello 1})
  (->MyException \"This is a message\" {:hello 1} (Exception. \"A cause\"))

  (ex-data (->MyException {:hello 1})) => {:hello 1}

  (ex-data (->MyException \"This is a message\" {:hello 1})) => {:hello 1}

  (.getMessage (->MyException \"This is a message\")) => \"This is a message\"

  or use the dynamically rebindable function *my-exception* to create and raise
  an exception with the possibility of being rebound to a function that fixes
  the problem.

  (*my-exception* \"message\" {:hello 1})  ;=> raises a MyException.

  (binding [*my-exception* (fn [message data] (do-something-with data))]
    ...
    (*my-exception* \"something went wrong\" {:maybe-this-helps 3}))

  or invoke the constructors directly

  (user/MyException. \"This is a Message\" {})
  (user/MyException. \"This is a Message\" {} (Exception. \"A message\"))"
  [t]
  (let [class-name (str (string/replace (str *ns*) "-" "_") "." t)
        ctor (symbol (str "->" t))
        dynamic (symbol (str "*" (impl/hyphenate t) "*"))]
    `(let [x# (impl/load-or-mk-ex-info-class ~class-name)]
       (import ~(symbol class-name))
       (defn ~ctor
         ([] (impl/make-ex x# nil {} nil))
         ([~'msg-o-data]
          (if (map? ~'msg-o-data)
            (impl/make-ex x# nil ~'msg-o-data nil)
            (impl/make-ex x# ~'msg-o-data {} nil)))
         ([~'msg ~'data] (impl/make-ex x# ~'msg ~'data nil))
         ([~'msg ~'data ~'cause] (impl/make-ex x# ~'msg ~'data ~'cause)))
       (defn ~(with-meta dynamic {:dynamic true}) [~'msg ~'data]
         (throw (~ctor ~'msg ~'data)))
       x#)))
