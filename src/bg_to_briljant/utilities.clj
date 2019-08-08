(ns bg-to-briljant.utilities
  (:require [clojure.string :as string]))

(defn capitalize-words
  "Capitalize every word in a string"
  [s]
  (->> (string/split (str s) #"\b")
       (map string/capitalize)
       string/join))

(defn tprn
  "Transparent clojure.core/prn."
  [arg]
  (prn arg)
  arg)


(defn condp-fn
  "Works like clojure.core/condp except that instead of a being a
  macro taking pairs of clauses it is a function taking a sequence
  of clauses."
  [predicate expr clauses  & [default]]
  (or (second (first (filter #(predicate (first %) expr)
                             (partition 2 clauses))))
      default))

(defn re-find-safe
  "Like re-find but returns nil instead of throwing."
  [regex string]
  (if (= (type string) java.lang.String)
    (re-find regex string)
    nil))
