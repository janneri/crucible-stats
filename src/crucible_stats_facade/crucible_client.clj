(ns crucible-stats-facade.crucible-client
  (:use [clojure.data.zip.xml :only (attr text xml1-> xml->)])
  (:require [cheshire.core :as json]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
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

(defn comments [review-id]
  (let [xml (zip/xml-zip (xml/parse (comments-uri review-id)))
        generalmessages (xml-> xml :generalCommentData :message text)
        generalusers (xml-> xml :generalCommentData :user text)
        linemessages (xml-> xml :versionedLineCommentData :message text)
        lineusers (xml-> xml :versionedLineCommentData :user text)
        messages (seq (concat generalmessages linemessages))
        users (seq (concat generalusers lineusers))]
    (for [i (range 0 (count messages))] 
      {:user (nth users i) :msg-word-count (word-count (nth messages i))})))
    ; XXX ugly
    ;(zipmap users messages))) ; will apply to linemessages only ???
    ;(merge (zipmap generalusers generalmessages) (zipmap lineusers linemessages))))
    ;(for [[user msg] (zipmap users messages)] {:user user :msg msg})))

(defn reviews []
  (let [json-data (json/parse-string (:body (client/get (reviews-uri))) true)
        reviews (:reviews json-data)]
    reviews))