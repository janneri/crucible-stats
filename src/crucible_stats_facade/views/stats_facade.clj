(ns crucible-stats-facade.views.stats_facade
  (:require [cheshire.core :as json]
            [crucible-stats-facade.review_cache :as cache])
  (:use [noir.core :only [defpage]]
        [crucible-stats-facade.utils]))

(defn count-total-comments-by-users []
  (let [comment-maps (mapcat :comments (get-comments))]
    (apply merge-with + comment-maps)))

; todo: not needed?
(defn get-review-comments [review-id]
  (let [review (find-first #(= review-id (:review-id %)) (cache/get-comments))]
    (apply merge (:comments review))))

(defn review-date [review]
  (to-date (get-in review [:reviewData :createDate])))

(defn review-dates [review-vector]
  (map review-date review-vector))

(defn group-reviews-by-month [review-vector]
  (for [[[year month] dates] (group-by (juxt :year :month)
                                       (review-dates review-vector))]
    {:year year
     :month month
     :count (count dates)}))

(defn group-reviews-by-author [review-vector]
  (for [[author count] (->> review-vector
                         (map (comp :userName :author :reviewData))
                         frequencies)]
    {:author author, :count count}))

(defn project-filter [predicate-fn projects-str review]
  (if-let [project-keys (null-safe-split projects-str #"," false)]
    (predicate-fn project-keys (get-in review [:reviewData :projectKey]))
    true))

(defn since-filter [since-str review]
  (if since-str
    (> 0 (compare since-str (to-str (review-date review))))
    true))

(defpage "/update-cache" {:keys [username password]}
  (cache/update-cache username password)
  (json/encode {:reviewsloaded (count (cache/get-reviews))}))
    
(defpage "/reviews-per-month" {:keys [excludedProjects includedProjects sinceDate]}
  (json/encode 
    (group-reviews-by-month 
      (filter (every-pred (partial since-filter sinceDate)
                          (partial project-filter not-in? excludedProjects)
                          (partial project-filter in? includedProjects))
              (get-reviews)))))
