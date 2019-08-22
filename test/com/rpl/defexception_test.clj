(ns com.rpl.defexception-test
  (:require [clojure.test :refer :all]
            [com.rpl.defexception :refer [defexception]]
            [clojure.java.io :as io]))

(defexception MyException)

(binding [*compile-files* true]
  (defexception TestException))

(deftest dynamic-exception-class-test
  (let [ex (MyException. "Message" {:hello 1})]
    (is (= "Message" (.getMessage ex)))
    (is (= {:hello 1} (ex-data ex))))
  (let [cause (Exception. "Cause")
        ex (MyException. "Message" {:hello 1} cause)]
    (is (= "Message" (.getMessage ex)))
    (is (= {:hello 1} (ex-data ex)))
    (is (= cause (.getCause ex)))))

(deftest dynamic-exception-class-const-fn
  (let [ex (->MyException)]
    (is (= nil (.getMessage ex)))
    (is (= {} (ex-data ex))))
  (let [ex (->MyException "Message")]
    (is (= "Message" (.getMessage ex)))
    (is (= {} (ex-data ex))))
  (let [ex (->MyException {:hello 1})]
    (is (= nil (.getMessage ex)))
    (is (= {:hello 1} (ex-data ex))))
  (let [ex (->MyException "Message" {:hello 1})]
    (is (= "Message" (.getMessage ex)))
    (is (= {:hello 1} (ex-data ex))))
  (let [cause (Exception. "Cause")
        ex (->MyException "Message" {:hello 1} cause)]
    (is (= "Message" (.getMessage ex)))
    (is (= {:hello 1} (ex-data ex)))
    (is (= cause (.getCause ex)))))

(deftest has-aoted
  (is (..
       (io/file *compile-path* "com/rpl/defexception_test/TestException.class")
       exists)))

(deftest aot-class-test
  (let [ex (->TestException)]
    (is (= nil (.getMessage ex)))
    (is (= {} (ex-data ex))))
  (let [ex (->TestException "Message")]
    (is (= "Message" (.getMessage ex)))
    (is (= {} (ex-data ex))))
  (let [ex (->TestException {:hello 1})]
    (is (= nil (.getMessage ex)))
    (is (= {:hello 1} (ex-data ex))))
  (let [ex (->TestException "Message" {:hello 1})]
    (is (= "Message" (.getMessage ex)))
    (is (= {:hello 1} (ex-data ex))))
  (let [cause (Exception. "Cause")
        ex (->TestException "Message" {:hello 1} cause)]
    (is (= "Message" (.getMessage ex)))
    (is (= {:hello 1} (ex-data ex)))
    (is (= cause (.getCause ex)))))
