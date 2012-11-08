(ns crucible-stats-facade.domain_commons
  (:use [crucible-stats-facade.utils]))

(defn id [review]
  (get-in review [:reviewData :permaId :id]))

(defn create-date [review]
  (to-date (get-in review [:reviewData :createDate])))