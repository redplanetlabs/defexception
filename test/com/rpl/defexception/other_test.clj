(ns com.rpl.defexception.other-test
  (:require [clojure.test :refer :all]
            [com.rpl.defexception-test :refer [->TestException]])
  (:import [com.rpl.defexception_test TestException]))

;; this is simply testing the import of an exception from a namespace
;; where it's defined

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



