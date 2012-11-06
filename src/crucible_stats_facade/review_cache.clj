(ns crucible-stats-facade.review_cache
  (:require [crucible-stats-facade.crucible-client :as client])
  (:use [clj-time.local :only [local-now]]
        [crucible-stats-facade.utils]))

(def cached-data (atom {:reviews-updated nil :reviews nil
                        :comments-updated nil :comments nil}))

(defn cache-status []
  {:reviews-updated (:reviews-updated @cached-data)
   :review-count (count (:reviews @cached-data))
   :comments-updated (:comments-updated @cached-data)})

(defn update-cached-reviews []
  (swap! cached-data assoc :reviews-updated (now) :reviews (client/reviews)))

(defn get-reviews []
  (if-let [reviews (:reviews @cached-data)]
    reviews
    (:reviews (update-cached-reviews))))

(defn get-review-ids []
  (map #(get-in % [:reviewData :permaId :id]) (get-reviews)))

(defn user-of-comment [comment-data]
  (if (:generalCommentData comment-data) 
    (get-in comment-data [:generalCommentData :user :userName])
    (get-in comment-data [:versionedLineCommentData :user :userName])))

(defn count-comments-by-users [comments]
  (map (fn [[name count]] {(keyword name) count}) 
       (frequencies (map user-of-comment comments))))

(defn comment-stats-for-review [review-id]
  (let [comments (client/comments review-id)]
    {:review-id review-id
     :comment-count (count comments)
     :comments (count-comments-by-users comments)}))

(defn comment-stats-for-all [review-ids]
  (map comment-stats-for-review review-ids))

(defn update-cached-comments [review-ids]
  (swap! cached-data assoc :comments-updated (now) 
         :comments (comment-stats-for-all review-ids)))

(defn get-comments []
  (if-let [comments (:comments @cached-data)]
    comments
    (:comments (update-cached-comments (get-review-ids)))))

(defn update-cache [username password]
  (println "updating cache started at" (local-now))  
  (client/login-and-get-token username password)
  (update-cached-reviews)
  (update-cached-comments (get-review-ids))
  (println "done at" (local-now)))