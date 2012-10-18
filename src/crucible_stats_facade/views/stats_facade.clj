(ns crucible-stats-facade.views.stats_facade
  (:require [cheshire.core :as json]
            [crucible-stats-facade.crucible-mockclient :as client])
  (:use [noir.core :only [defpage]]))


(defn in? [seq elm]
  (some #(= elm %) seq))

(defn not-in? [seq elm]
  (not (in? seq elm)))

(defn null-safe-split [str pattern default-if-null]
  (if str
     (clojure.string/split str pattern)
     default-if-null))

(defn to-date [date-str]
  (identity {:year (.substring date-str 0 4)
             :month (.substring date-str 5 7)
             :date (.substring date-str 8 10)}))
  
(defn review-dates [review-vector]
  (map (fn [review] (to-date (get-in review [:reviewData :createDate]))) review-vector))

(defn group-reviews-by-month [review-vector]
  (map (fn [pair] {:year (first (first pair)) :month (second (first pair)) :count (count (second pair))}) 
        (group-by (juxt :year :month) (review-dates review-vector))))

(defn group-reviews-by-author [review-vector]
  (let [authors (map (fn [review] (get-in review [:reviewData :author :userName])) review-vector)]
        (map (fn [pair] {:author (first pair) :count (second pair)}) (frequencies authors))))

(defn included-by-project? [excluded-project-keys review]
  (not-in? excluded-project-keys (get-in review [:reviewData :projectKey])))

(defn project-key-filter [excluded-projects-str]
  (if-let [excluded-project-keys (null-safe-split excluded-projects-str #"," [])]
    (partial included-by-project? excluded-project-keys)
    (fn [review] true)))

(defn create-filters [excluded-projects-str since-str] ; todo sincedate
  (project-key-filter excluded-projects-str))



(defpage "/reviews-per-month" {:keys [excludedProjects sinceDate]}
  (json/encode 
    (group-reviews-by-month 
      (filter (create-filters excludedProjects sinceDate) (client/reviews)))))

