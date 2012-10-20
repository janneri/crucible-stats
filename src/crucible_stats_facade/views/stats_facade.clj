(ns crucible-stats-facade.views.stats_facade
  (:require [cheshire.core :as json]
            [crucible-stats-facade.crucible-mockclient :as client])
  (:use [noir.core :only [defpage]]
        [crucible-stats-facade.utils]))

(def cached-reviews (atom {:updated nil :reviews nil}))

(defn update-cached-reviews []
  (swap! cached-reviews assoc :updated (now) :reviews (client/reviews)))

(defn get-reviews []
  (if-let [reviews (:reviews @cached-reviews)]
    reviews
    (:reviews (update-cached-reviews))))

(defn review-date [review]
  (to-date (get-in review [:reviewData :createDate])))

(defn review-dates [review-vector]
  (map (fn [review] (review-date) review-vector)))

(defn group-reviews-by-month [review-vector]
  (map (fn [[[y m] s]] {:year y :month m :count (count s)}) 
    (group-by (juxt :year :month) (review-dates review-vector))))

(defn group-reviews-by-author [review-vector]
  (let [authors (map (fn [review] (get-in review [:reviewData :author :userName])) review-vector)]
        (map (fn [pair] {:author (first pair) :count (second pair)}) (frequencies authors))))

(defn included-by-project? [excluded-project-keys review]
  (not-in? excluded-project-keys (get-in review [:reviewData :projectKey])))

(defn project-key-filter [excluded-projects-str review]
  (if-let [excluded-project-keys (null-safe-split excluded-projects-str #"," [])]
    (included-by-project? excluded-project-keys review)
    (fn [review] true)))

(defn since-filter [since-str review]
  (if since-str
    (> 0 (compare since-str (to-str (review-date review))))
    (fn [review] true)))

(defn combine-filters [filters review] 
  (every? #(= true %) (map #(% review) filters)))

(defn create-filters [excluded-projects-str since-str review]
  (combine-filters [(partial since-filter since-str) 
                    (partial project-key-filter excluded-projects-str)] 
    review))
  
(defpage "/reviews-per-month" {:keys [excludedProjects sinceDate]}
  (json/encode 
    (group-reviews-by-month 
      (filter #(create-filters excludedProjects sinceDate %) (get-reviews)))))

