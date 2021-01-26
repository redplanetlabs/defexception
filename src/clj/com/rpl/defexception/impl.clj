(ns ^:no-doc com.rpl.defexception.impl
  (:require [clojure.string :as string])
  (:import [clojure.asm MethodVisitor
            ClassVisitor ClassWriter Opcodes Type]
           [clojure.asm.commons GeneratorAdapter Method]
           [clojure.lang ExceptionInfo]
           [com.rpl.defexception IDefException]))

(defn- forward-constructor [^ClassWriter cw ^Type t ^Method constr]
  (doto (GeneratorAdapter. Opcodes/ACC_PUBLIC constr nil nil cw)
    (.loadThis)
    (.loadArgs)
    (.invokeConstructor t constr)
    (.returnValue)
    (.endMethod)))

(defn- to-string-method [^ClassWriter cw ^String exception-class-name]
  (let [string (Type/getType String)
        string-builder (Type/getType StringBuilder)
        string-append (Method/getMethod
                        "java.lang.StringBuilder append (java.lang.String)")
        to-string (Method/getMethod "java.lang.String toString ()")]
    (doto (GeneratorAdapter. Opcodes/ACC_PUBLIC to-string nil nil cw)
      (.newInstance string)
      (.dup)

      (.newInstance string-builder)
      (.dup)
      (.invokeConstructor string-builder (Method/getMethod "void <init> ()"))

      (.push (str exception-class-name ": "))
      (.invokeVirtual string-builder string-append)

      (.loadThis)
      (.invokeVirtual (Type/getType Exception) (Method/getMethod "String getMessage ()"))
      (.invokeVirtual string-builder string-append)

      (.push " ")
      (.invokeVirtual string-builder string-append)

      (.loadThis)
      (.getField (Type/getType clojure.lang.ExceptionInfo)
                 "data"
                 (Type/getType clojure.lang.IPersistentMap))
      (.invokeVirtual (Type/getType Object) to-string)
      (.invokeVirtual string-builder string-append)

      (.invokeConstructor
        string
        (Method/getMethod "void <init> (java.lang.StringBuilder)"))

      (.returnValue)
      (.endMethod))))

(defn- define-class [^clojure.lang.DynamicClassLoader cl ^String name ^ClassWriter cw]
  (let [klass (.defineClass cl name (.toByteArray cw) nil)]
    (when *compile-files*
      (clojure.lang.Compiler/writeClassFile
       (string/replace (munge (.getName klass)) "." "/")
       (.toByteArray cw)))
    klass))

(defn- mk-ex-info-class [exception-class-name]
  #_(prn :making exception-class-name)
  (let [cw (ClassWriter. (+ ClassWriter/COMPUTE_MAXS ClassWriter/COMPUTE_FRAMES))
        ex-info-type (Type/getType clojure.lang.ExceptionInfo)
        internal-name (string/replace exception-class-name "." "/")
        class-name (-> internal-name (string/split #"/") last)]
    (.visit cw
            ;; this allows compatibility back to Clojure 1.4
            ;; and is = clojure.asm.Opcodes/V1_7
            (inc clojure.asm.Opcodes/V1_6)
            (+ Opcodes/ACC_SUPER Opcodes/ACC_PUBLIC)
            internal-name
            nil
            (.getInternalName ex-info-type)
            (into-array String [(Type/getInternalName IDefException)]))
    (forward-constructor
      cw
      ex-info-type
      (Method/getMethod "void <init> (String, clojure.lang.IPersistentMap)"))
    (forward-constructor
      cw
      ex-info-type
      (Method/getMethod "void <init> (String, clojure.lang.IPersistentMap, Throwable)"))
    (to-string-method
      cw
      class-name)
    (.visitEnd cw)
    (define-class
      (clojure.lang.DynamicClassLoader.)
      exception-class-name
      cw)))

(defn load-or-mk-ex-info-class [exception-class-name]
  (try (clojure.lang.RT/classForName exception-class-name)
       (catch ClassNotFoundException e
         (mk-ex-info-class exception-class-name))))

(def ^:private ex-info-const
  (memoize
   (fn [^Class klass cause?]
     (let [arg-types [String clojure.lang.IPersistentMap]]
       (.getConstructor
        klass
        (into-array Class
                    (if cause?
                      (conj arg-types Throwable)
                      arg-types)))))))

(defn- fix-stack-trace [^ExceptionInfo ex-info-instance]
  (let [stacktrace (.getStackTrace ex-info-instance)
        constructor-pattern (re-pattern
                             (str ".*"
                                  (munge
                                   (str "->"
                                        (-> ex-info-instance
                                            class
                                            (.getSimpleName))))
                                  ".*"))
        updated-stacktrace (->> stacktrace
                                (drop-while
                                 (fn [^StackTraceElement elt]
                                   (not
                                    (re-matches
                                     constructor-pattern
                                     (.getClassName elt)))))
                                rest
                                (into-array StackTraceElement))]
    (when stacktrace
      (.setStackTrace ex-info-instance updated-stacktrace))
    ex-info-instance)
  )

(defn make-ex
  "use reflection to instantiate the exception class"
  [^Class klass msg data cause]
  (let [constr ^java.lang.reflect.Constructor (ex-info-const klass cause)
        args [msg (or data {})]]
    (fix-stack-trace (.newInstance constr
                                   (object-array
                                    (if cause
                                      (conj args cause)
                                      args))))))
