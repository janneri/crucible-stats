(ns crucible-stats-facade.views.stats_facade
  (:require [cheshire.core :as json]
            [crucible-stats-facade.review_cache :as cache])
  (:use [noir.core :only [defpage]]
        [crucible-stats-facade.utils]
        [crucible-stats-facade.domain_commons]))

(defn count-total-comments-by-users []
  (let [comment-maps (mapcat :comments (cache/get-comments))]
    (apply merge-with + comment-maps)))

; todo: not needed? multimethod in cache?
(defn get-review-comments [review-id]
  (let [review (find-first #(= review-id (:review-id %)) (cache/get-comments))]
    (apply merge (:comments review))))

(defn create-dates [review-vector]
  (map create-date review-vector))

(defn group-reviews-by-month [review-vector]
  (for [[[year month] dates] (group-by (juxt :year :month)
                                       (create-dates review-vector))]
    {:year year
     :month month
     :count (count dates)}))

(defn group-reviews-by-author [review-vector]
  (for [[author count] (->> review-vector
                         (map author)
                         frequencies)]
    {:author author, :count count}))

(defn project-filter [predicate-fn projects-str review]
  (if-let [project-keys (null-safe-split projects-str #"," false)]
    (predicate-fn project-keys (project-key review))
    true))

(defn since-filter [since-str review]
  (if since-str
    (> 0 (compare since-str (to-str (create-date review))))
    true))

(defn comment-count-filter [comment-count-str review]
  (if comment-count-str
    (>= (count (get-review-comments (id review))) (read-string comment-count-str))
    true))


(defpage "/update-cache" {:keys [username password]}
  (cache/update-cache username password)
  (json/encode {:reviewsloaded (count (cache/get-reviews))}))
    
(defpage "/reviews-per-month" {:keys [excludedProjects includedProjects sinceDate minComments]}
  (json/encode 
    (group-reviews-by-month 
      (filter (every-pred (partial since-filter sinceDate)
                          (partial project-filter not-in? excludedProjects)
                          (partial project-filter in? includedProjects)
                          (partial comment-count-filter minComments))
              (cache/get-reviews)))))
