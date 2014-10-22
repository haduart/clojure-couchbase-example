(ns clojure-couchbase-example.core
  (:import [java.util Date])
  (:require [couchbase-clj.client :as c]
            [couchbase-clj.future :as f]
            [clj-time.core :as t]
            [clojure.data.json :as json]))

(defn- DateTime->millis [dateTime]
  (->
    dateTime
    (.getMillis)))

(defn decrement-seconds-to-date [startDate decrement]
  (.minusSeconds startDate decrement))

(def counter (atom 0))

(defn generate-random-value [value]
  (->
    (Math/sin value)
    (* 100)
    int
    (+ (rand-int 10))))

(defn generate-key-value-document [keyValue]
  (let [swapedValue (swap! counter inc)
        value (generate-random-value swapedValue)]
    [keyValue {:min value
               :max (+ value 100)}]))

(defn generate-dateTime-intervals [decrement]
  (->>
    (t/now)
    (iterate #(decrement-seconds-to-date % decrement))
    (map DateTime->millis)
    (map generate-key-value-document)))




(c/defclient client {:bucket "TagP"
                     :uris ["http://127.0.0.1:8091/pools"]})


;(def rob {:phone "1" :brand "note"})
;
;(c/add-json client "rob" rob)
;
;;; Specify a key and get a JSON string value that is converted to a Clojure data.
;(c/get-json client "rob")
;
;(def fut (c/async-get client "rob"))
;(f/deref-json fut)


(defn add-json-data! [entry]
  (c/add-json client (str (first entry)) (second entry)))


(defn run-benchmark [num-entries]
  (->>
    (generate-dateTime-intervals 15)
    (pmap add-json-data!)
    (take num-entries)
    time))

(defn -main [& [entries]]
  ;  (run-benchmark 2))
  (let [num-entries (Integer. (or entries (System/getenv "ENTRIES")))]
    (run-benchmark num-entries)))


