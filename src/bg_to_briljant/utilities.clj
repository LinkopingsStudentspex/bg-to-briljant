(ns bg-to-briljant.utilities
  (:require [clojure.string :as string]))

(defn capitalize-words
  "Capitalize every word in a string"
  [s]
  (->> (string/split (str s) #"\b")
       (map string/capitalize)
       string/join))

(defn tprn
  [arg]
  (prn arg)
  arg)
