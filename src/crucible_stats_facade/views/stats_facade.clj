(ns crucible-stats-facade.views.stats_facade
  (:require [cheshire.core :as json]
            [crucible-stats-facade.crucible-client :as client])
  (:use [noir.core :only [defpage]]
        [clj-time.local :only [local-now]]
        [crucible-stats-facade.utils]))

(def cached-data (atom {:reviews-updated nil :reviews nil
                        :comments-updated nil :comments nil}))

(defn cache-status []
  {:reviews-updated (:reviews-updated @cached-data)
   :review-count (count (:reviews @cached-data))
   :comments-updated (:comments-updated @cached-data)})

(declare comment-stats-for-all get-review-ids)

(defn update-cached-comments [review-ids]
  (swap! cached-data assoc :comments-updated (now) 
         :comments (comment-stats-for-all review-ids)))

(defn update-cached-reviews []
  (swap! cached-data assoc :reviews-updated (now) :reviews (client/reviews)))


; comments

(defn user-of-comment [comment-data]
  (if (:generalCommentData comment-data) 
    (get-in comment-data [:generalCommentData :user :userName])
    (get-in comment-data [:versionedLineCommentData :user :userName])))

(defn count-comments-by-users [review-id]
  (map (fn [[name count]] {(keyword name) count}) 
       (frequencies (map user-of-comment (client/comments review-id)))))

(defn comment-stats-for-review [review-id]
  {:review-id review-id :comments (count-comments-by-users review-id)})

(defn comment-stats-for-all [review-ids]
  (map comment-stats-for-review review-ids))

(defn get-comments []
  (if-let [comments (:comments @cached-data)]
    comments
    (:comments (update-cached-comments (get-review-ids)))))

(defn count-total-comments-by-users []
  (let [comment-maps (mapcat :comments (get-comments))]
    (apply merge-with + comment-maps)))


; reviews

(defn get-reviews []
  (if-let [reviews (:reviews @cached-data)]
    reviews
    (:reviews (update-cached-reviews))))

(defn get-review-ids []
  (map #(get-in % [:reviewData :permaId :id]) (get-reviews)))

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
  (println "updating cache started at" (local-now))  
  (client/login-and-get-token username password)
  (update-cached-reviews)
  (update-cached-comments (get-review-ids))
  (println "done at" (local-now)))
    
(defpage "/reviews-per-month" {:keys [excludedProjects includedProjects sinceDate]}
  (json/encode 
    (group-reviews-by-month 
      (filter (every-pred (partial since-filter sinceDate)
                          (partial project-filter not-in? excludedProjects)
                          (partial project-filter in? includedProjects))
              (get-reviews)))))
