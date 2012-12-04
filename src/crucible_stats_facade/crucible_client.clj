(ns crucible_stats_facade.crucible-client
  (:use [clojure.data.zip.xml :only (attr text xml1-> xml->)])
  (:require [cheshire.core :as json]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clj-http.client :as client]))

(def token (atom {}))

(defn token-param []
  (if-let [tokenStr (:tokenStr @token)]
    (str "FEAUTH=" tokenStr)
    (throw (IllegalStateException. "login required (did you update the cache)"))))

(def valid-review-states "Draft,Approval,Review,Summarize,Closed")

(def base-uri "http://localhost:8080/rest-service/")
(defn login-uri [username password] (str base-uri "auth-v1/login?userName=" username "&password=" password))
(defn projects-uri [] (str base-uri "projects-v1?" (token-param)))
(defn reviews-uri [] (str base-uri "reviews-v1?" (token-param) "&state=" valid-review-states))
(defn comments-uri [review-id] (str base-uri "reviews-v1/" review-id "/comments?" (token-param)))


(defn login [username password]
  (xml/parse (login-uri "foo" "bar")))

(defn parse-token [xml]
  (let [zipped (zip/xml-zip xml)]
    (xml1-> zipped :token text)))

(defn login-and-get-token [username password]
  (let [new-token (parse-token (login username password))]
    (swap! token assoc :tokenStr new-token))) 
        
(defn project-ids []
  (let [json-data (json/parse-string (:body (client/get (projects-uri))) true)
        projects (:projects json-data)]
    (map (fn [pdata] (get-in pdata [:projectData :key])) projects)))


(defn json-comments [review-id]
  (let [json-data (json/parse-string (:body (client/get (comments-uri review-id))) true)
        comments (:comments json-data)]
    comments))

(defn word-count [msg]
  (count (clojure.string/split msg #" ")))

(defn username [comment]
  (if-let [username (xml1-> comment :user :userName text)]
    username
    "unknown"))

(defn comment-xml-to-comment-map [comment-xml]
  (let [message (xml1-> comment-xml :message text)
        user (username comment-xml)]
    {:user user :msg-word-count (word-count message)}))
    
(defn parse-comments [root-comment]
  (let [replies (xml-> root-comment :replies :generalCommentData)
        valid (= "false" (xml1-> root-comment :deleted text))
        comment (comment-xml-to-comment-map root-comment)]
    (if valid 
      (if (empty? replies)
        (list comment)
        (conj (mapcat parse-comments replies) comment))
      '())))

(defn comments [review-id]
  (let [xml (zip/xml-zip (xml/parse (comments-uri review-id)))
        general (mapcat parse-comments (xml-> xml :generalCommentData))
        line (mapcat parse-comments (xml-> xml :versionedLineCommentData))]
    (concat general line)))

(defn reviews []
  (let [json-data (json/parse-string (:body (client/get (reviews-uri))) true)
        reviews (:reviews json-data)]
    (for [review (map :reviewData reviews)] 
      {:id ((comp :id :permaId) review)
       :projectKey (:projectKey review)
       :author ((comp :userName :author) review)
       :createDate (:createDate review)})))

