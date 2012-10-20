(ns crucible-stats-facade.crucible-mockclient
  (:require [cheshire.core :as json]
            [clojure.xml :as xml]
            [clj-http.client :as client]))

(def token (atom {}))

(defn token-param []
  (if-let [tokenStr (:tokenStr @token)]
    (str "FEAUTH=" tokenStr)
    (throw (IllegalStateException. "login required"))))

(def valid-review-states "Draft,Approval,Review,Summarize,Closed")

(def base-uri "http://localhost:8080/rest-service/")
(defn login-uri [username password] (str base-uri "auth-v1/login?userName=" username "&password=" password))
(def projects-uri (str base-uri "projects-v1?" token-param))
(def reviews-uri (str base-uri "reviews-v1?" token-param "&state=" valid-review-states))
(defn comments-uri [review-id] (str base-uri "reviews-v1/" review-id "/comments?" token-param))


(defn login [username password]
  (xml/parse (login-uri "foo" "bar")))

(defn parse-token [xml]
  (first (for [x (xml-seq xml) :when (= :token (:tag x))]
           (first (:content x)))))

(defn login-and-get-token [username password]
  (let [new-token (parse-token (login username password))]
    (swap! token assoc :tokenStr new-token))) 
        
(defn project-ids []
  (let [json-data (json/parse-string (:body (client/get projects-uri)) true)
        projects (:projects json-data)]
    (map (fn [pdata] (get-in pdata [:projectData :key])) projects)))
                   
(defn user-of-comment [comment-data]
  (if (:generalCommentData comment-data) 
    (get-in comment-data [:generalCommentData :user :userName])
    (get-in comment-data [:versionedLineCommentData :user :userName])))

(defn comments [review-id]
  (let [json-data (json/parse-string (:body (client/get (comments-uri review-id))) true)
        comments (:comments json-data)]
    comments))

;(defn comment-count-by-users [review-ids]
;  (map #() (map user-of-comment (comments "foo"))))

(defn reviews []
  (let [json-data (json/parse-string (:body (client/get reviews-uri)) true)
        reviews (:reviews json-data)]
    reviews))