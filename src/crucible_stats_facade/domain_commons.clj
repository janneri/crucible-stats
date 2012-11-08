(ns crucible-stats-facade.domain_commons
  (:use [crucible-stats-facade.utils]))

(defn id [review]
  (get-in review [:reviewData :permaId :id]))

(defn create-date [review]
  (to-date (get-in review [:reviewData :createDate])))

(defn project-key [review]
  (get-in review [:reviewData :projectKey]))

(defn author [review]
  ((comp :userName :author :reviewData) review))