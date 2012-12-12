(ns crucible_stats_facade.review_cache
  (:require [crucible_stats_facade.crucible-client :as client])
  (:use [clj-time.local :only [local-now]]
        [crucible_stats_facade.utils]))

(def cached-data (atom {:reviews-updated nil :reviews nil
                        :comments-updated nil :comments nil}))

(defn cache-status []
  {:reviews-updated (:reviews-updated @cached-data)
   :review-count (count (:reviews @cached-data))
   :comments-updated (:comments-updated @cached-data)
   :last-update-started (:last-update-started @cached-data)})

(defn update-cached-reviews [since-date]
  (swap! cached-data assoc :reviews-updated (now) 
         :reviews (filter (partial created-since-filter since-date) (client/reviews))))

(defn get-reviews []
  (if-let [reviews (:reviews @cached-data)]
    reviews
    []))

(defn get-review-ids []
  (map :id (get-reviews)))

(defn comments-for-review [review-id]
  (let [comments (client/comments review-id)]
    {:review-id review-id
     :comment-count (count comments)
     :comments comments}))

(defn comments-for-all [review-ids]
  (map comments-for-review review-ids))

(defn update-cached-comments [review-ids]
  (swap! cached-data assoc :comments-updated (now) 
         :comments (comments-for-all review-ids)))

(defn get-comments []
  (if-let [comments (:comments @cached-data)]
    comments
    []))

(defn update-cache [username password since-date]
  (println "updating cache started at" (local-now))
  (swap! cached-data assoc :last-update-started (now))
  (Thread/sleep 5000) ;todo remove  
  (client/login-and-get-token username password)
  (println "got token" (client/token-param))
  (update-cached-reviews since-date)
  (update-cached-comments (get-review-ids))
  (println "done loading" (count (get-reviews)) "reviews at" (local-now)))
