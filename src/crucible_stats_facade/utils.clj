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

(defn to-year-month [datetime]
  {:year (.getYear datetime)
   :month (.getMonthOfYear datetime)})
  
(defn to-str [date-map]
  (str (:year date-map) "-" (:month date-map) "-" (:date date-map)))

(defn to-datetime-midnight [^String date-str]
  (if (empty? date-str) nil (org.joda.time.DateTime. date-str)))

(defn roll-month [date-map]
  (if (= 12 (:month date-map)) 
    (assoc date-map :year (inc (:year date-map)) :month 1)
    (update-in date-map [:month] inc)))

(defn now []
  (java.util.Date.))

(defn nvl [value default-if-null]
  (if value value default-if-null))

(defn created-since-filter [since-date review]
  (if since-date
    (.isBefore since-date (:createDate review))
    true))
