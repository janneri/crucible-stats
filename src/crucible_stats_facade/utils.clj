(ns crucible_stats_facade.utils)

(defn in? [seq elm]
  (some #(= elm %) seq))

(defn not-in? [seq elm]
  (not (in? seq elm)))

(defn find-first [pred col]
  (first (filter pred col)))

(defn null-safe-split [str pattern default-if-null]
  (if str
     (clojure.string/split str pattern)
     default-if-null))

(defn to-date [date-str]
  (identity {:year (.substring date-str 0 4)
             :month (.substring date-str 5 7)
             :date (.substring date-str 8 10)}))

(defn to-str [date-map]
  (str (:year date-map) "-" (:month date-map) "-" (:date date-map)))

(defn now []
  (java.util.Date.))

(defn nvl [value default-if-null]
  (if value value default-if-null))
