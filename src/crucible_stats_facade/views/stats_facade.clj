(ns crucible_stats_facade.views.stats_facade
  (:require [cheshire.core :as json]
            [crucible_stats_facade.review_cache :as cache])
  (:use [noir.core :only [defpage]]
        [crucible_stats_facade.utils]))

(defn comments-for-review-ids [review-ids]
  (filter #(in? review-ids (:review-id %)) (cache/get-comments)))

(defn comments-for-reviews [review-vector]
  (comments-for-review-ids (map :id review-vector)))

(defn group-reviews-by-month [review-vector]
  (for [[[year month] dates] (group-by (juxt :year :month)
                                       (map (comp to-date :createDate) review-vector))]
    {:year year
     :month month
     :count (count dates)}))

(defn group-reviews-by-author [review-vector]
  (for [[author count] (->> review-vector
                         (map :author)
                         frequencies)]
    {:author author, :count count}))

(defn avg-comment-length-by-users [review-vector]
  (let [comments (mapcat :comments (comments-for-reviews review-vector))
        users (set (map :user comments))
        comments-of-usr (fn [user clist] (filter #(= user (:user %)) clist))]
    (map (fn [user] {:username user
                     :avg (/ (apply + (map :msg-word-count (comments-of-usr user comments)))
                             (count (comments-of-usr user comments)))}) 
      users)))

(defn count-comments-by-users [review-vector]
  (let [usernames (map :user (mapcat :comments (comments-for-reviews review-vector)))]
    (map (fn [[name count]] {:username name :commentcount count}) 
       (frequencies usernames))))

(defn review-count-of-author [count-map username]
  (:count (find-first #(= username (:author %)) count-map)))

(defn avg-msg-length-of-author [avg-length-map username]
  (:avg (find-first #(= username (:username %)) avg-length-map)))

(defn user-stats [review-vector]
  (let [author-stats (group-reviews-by-author review-vector)
        avg-stats (avg-comment-length-by-users review-vector)]
    (for [stat (count-comments-by-users review-vector)] 
      (assoc stat :authoredreviewcount (review-count-of-author author-stats (:username stat))
                  :avgmessagelength (avg-msg-length-of-author avg-stats (:username stat))))))

(defn project-filter [predicate-fn projects-str review]
  (if-let [project-keys (null-safe-split projects-str #"," false)]
    (predicate-fn project-keys (:projectKey review))
    true))

(defn created-since-filter [since-str review]
  (if since-str
    (> 0 (compare since-str (to-str (to-date (:createDate review)))))
    true))

(defn author-filter [authors-str review]
  (if-let [authors (null-safe-split authors-str #"," false)]
    (in? authors (:author review))
    true))

(defn comment-count-filter [comment-count-str review]
  (if comment-count-str
    (>= (:comment-count (first (comments-for-review-ids [(:id review)]))) 
        (read-string comment-count-str))
    true))

(defn commented-filter [usernames-str review]
  (if-let [usernames (null-safe-split usernames-str #"," false)]
    (in? usernames (:author review))
    true))

(defn filtered-reviews [params]
  (let [{:keys [excludedProjects includedProjects authors sinceDate minComments]} params]
    (filter 
	    (every-pred 
	      (partial created-since-filter sinceDate)
	      (partial project-filter not-in? excludedProjects)
	      (partial project-filter in? includedProjects)
        (partial author-filter authors)
	      (partial comment-count-filter minComments))
	    (cache/get-reviews))))

(defpage [:post "/update-cache"] {:keys [username password]}
  (Thread/sleep 5000)
  (cache/update-cache username password)
  (json/encode {:reviewsloaded (count (cache/get-reviews))}))

(defpage "/cache-status" params
  (json/encode (cache/cache-status))) 

(defpage "/reviews" params
  (json/encode 
    (filtered-reviews params)))

(defpage "/reviews-per-month" params
  (json/encode 
    (group-reviews-by-month 
      (filtered-reviews params))))

(defpage "/comments" params
  (json/encode 
    (comments-for-reviews 
      (filtered-reviews params))))

(defpage "/userstats" params
  (json/encode 
    (user-stats 
      (filtered-reviews params))))

