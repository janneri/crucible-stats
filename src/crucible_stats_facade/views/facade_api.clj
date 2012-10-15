(ns crucible-stats-facade.views.facade-api
  (:require [cheshire.core :as json]
            [crucible-stats-facade.crucible-mockclient :as client])
  (:use [noir.core :only [defpage]]))

(defn group-reviews-by-month [review-vector]
  (map (fn [pair] {:year (first (first pair)) :month (second (first pair)) :count (count (second pair))}) 
        (group-by (juxt :year :month) (review-months review-vector))))

(defn review-months [review-vector]
  (let [createDates (map (fn [review] (get-in review [:reviewData :createDate])) review-vector)]
        (map (fn [dateStr] {:year (.substring dateStr 0 4) 
                            :month (.substring dateStr 5 7)}) createDates)))

(defn group-reviews-by-author [review-vector]
  (let [authors (map (fn [review] (get-in review [:reviewData :author :userName])) review-vector)]
        (map (fn [pair] {:author (first pair) :count (second pair)}) (frequencies authors))))

(defpage "/reviews-per-month" []
         (json/encode (group-reviews-by-month (client/reviews))))