(ns crucible-stats-facade.views.stats_facade
  (:require [cheshire.core :as json]
            [crucible-stats-facade.crucible-client :as client])
  (:use [noir.core :only [defpage]]
        [crucible-stats-facade.utils]))

(def cached-data (atom {:reviews-updated nil :reviews nil
                        :comments-updated nil :comments nil}))

(defn cache-status []
  {:reviews-updated (:reviews-updated @cached-data)
   :review-count (count (:reviews @cached-data))
   :comments-updated (:comments-updated @cached-data)})

(declare comment-stats-for-all)

(defn update-cached-comments [reviews]
  (swap! cached-data assoc :comments-updated (now) 
         :comments (comment-stats-for-all reviews)))

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
    true))

(defn since-filter [since-str review]
  (if since-str
    (> 0 (compare since-str (to-str (review-date review))))
    true))

(defn combine-filters [filters review] 
  (every? #(= true %) (map #(% review) filters)))

(defn create-filters [excluded-projects-str since-str review]
  (combine-filters [(partial since-filter since-str) 
                    (partial project-key-filter excluded-projects-str)] 
    review))


;(defpage "/update-cache" {:keys [sinceDate]}
;  (json/encode 
    
(defpage "/reviews-per-month" {:keys [excludedProjects sinceDate]}
  (json/encode 
    (group-reviews-by-month 
      (filter #(create-filters excludedProjects sinceDate %) (get-reviews)))))

