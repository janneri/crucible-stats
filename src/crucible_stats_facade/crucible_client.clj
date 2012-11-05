(ns crucible-stats-facade.crucible-client
  (:require [cheshire.core :as json]
            [clojure.xml :as xml]
            [clj-http.client :as client]))

(defonce token (atom {}))

(defn token-param []
  (if-let [tokenStr (:tokenStr @token)]
    (str "FEAUTH=" tokenStr)
    (throw (IllegalStateException. "login required"))))

(def valid-review-states "Draft,Approval,Review,Summarize,Closed")

(def base-uri "http://localhost:8090/rest-service/")
(defn login-uri [username password] (str base-uri "auth-v1/login?userName=" username "&password=" password))
(defn projects-uri [] (str base-uri "projects-v1?" (token-param)))
(defn reviews-uri [] (str base-uri "reviews-v1?" (token-param) "&state=" valid-review-states))
(defn comments-uri [review-id] (str base-uri "reviews-v1/" review-id "/comments?" (token-param)))


(defn login [username password]
  (xml/parse (login-uri "foo" "bar")))

(defn parse-token [xml]
  (first (for [x (xml-seq xml) :when (= :token (:tag x))]
           (first (:content x)))))

(defn login-and-get-token [username password]
  (let [new-token (parse-token (login username password))]
    (swap! token assoc :tokenStr new-token))) 
        
(defn project-ids []
  (let [json-data (json/parse-string (:body (client/get (projects-uri))) true)
        projects (:projects json-data)]
    (map (fn [pdata] (get-in pdata [:projectData :key])) projects)))


(defn comments [review-id]
  (let [json-data (json/parse-string (:body (client/get (comments-uri review-id))) true)
        comments (:comments json-data)]
    comments))

(defn reviews []
  (let [json-data (json/parse-string (:body (client/get (reviews-uri))) true)
        reviews (:reviews json-data)]
    reviews))