(ns crucible_stats_facade.views.stats_facade
  (:require [cheshire.core :as json]
            [crucible_stats_facade.review_cache :as cache]
            [noir.response :as resp])
  (:use [noir.core :only [defpage]]
        [crucible_stats_facade.utils]))

(defn comments-for-review [review]
  (find-first #(= (:id review) (:review-id %)) (cache/get-comments)))

(defn comments-for-reviews [review-vector]
  (->> review-vector
    (map comments-for-review)
    (mapcat :comments)))

(defn year-month [ym] 
  ((juxt :year :month) ym))
  
(defn pad-missing-months [accu reviews-by-month]
  (let [equal-year-month (fn [m1 m2] (= (year-month m1) (year-month m2)))
        prev-rolled (-> (last accu) (roll-month) (assoc :count 0))
        current (first reviews-by-month)]
    (if (empty? reviews-by-month)
      accu
      (if (equal-year-month prev-rolled current)
        (pad-missing-months (conj accu current) (drop 1 reviews-by-month))
        (pad-missing-months (conj accu prev-rolled) reviews-by-month)))))
      

(defn group-reviews-by-month [review-vector]
  (let [grouped 
    (sort-by (juxt :year :month) 
      (for [[[year month] dates] (->> review-vector
                                 (map (comp to-year-month :createDate))
                                 (group-by (juxt :year :month)))]
      {:year year
       :month month
       :count (count dates)}))]
    (if (empty? grouped) 
      []
      (pad-missing-months [(first grouped)] (drop 1 grouped)))))
  

(defn group-reviews-by-author [review-vector]
  (for [[author count] (->> review-vector
                         (map :author)
                         frequencies)]
    {:author author, :count count}))

(defn avg-comment-length-by-users [review-vector]
  (let [comments (comments-for-reviews review-vector)
        users (set (map :user comments))
        comments-of-usr (fn [user clist] (filter #(= user (:user %)) clist))]
    (map (fn [user] {:username user
                     :avg (/ (apply + (map :msg-word-count (comments-of-usr user comments)))
                             (count (comments-of-usr user comments)))}) 
      users)))

(defn count-comments-by-users [review-vector]
  (for [[username commentcount] (->> (comments-for-reviews review-vector)
                                  (map :user)
                                  frequencies)]
    {:username username :commentcount commentcount}))

(defn review-count-of-author [count-map username]
  (nvl (:count (find-first #(= username (:author %)) count-map)) 0))

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

(defn author-filter [authors-str review]
  (if-let [authors (null-safe-split authors-str #"," false)]
    (in? authors (:author review))
    true))

(defn comment-count-filter [comment-count-str review]
  (if comment-count-str
    (>= (:comment-count (comments-for-review review)) 
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
	      (partial created-since-filter (to-datetime-midnight sinceDate))
	      (partial project-filter not-in? excludedProjects)
	      (partial project-filter in? includedProjects)
        (partial author-filter authors)
	      (partial comment-count-filter minComments))
	    (cache/get-reviews))))

(defpage [:post "/update-cache"] {:keys [username password sinceDate]}
  (cache/update-cache username password (to-datetime-midnight sinceDate))
  (json/encode (cache/cache-status)))

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

(defpage "/stats" params
  (json/encode
    (let [reviews (filtered-reviews params)]
      {:reviews (map #(assoc % :createDate (.toDate (:createDate %))) reviews)
       :monthlyStats (group-reviews-by-month reviews)
       :userStats (user-stats reviews)}))) 

(defpage "/" []
  (resp/redirect "/index.html"))