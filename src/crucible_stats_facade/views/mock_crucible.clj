(ns crucible_stats_facade.views.mock-crucible
  (:require [cheshire.core :as json])
  (:use [noir.core :only [defpage]]))


(def mock-token "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>
     <loginResult><token>evzijst:3319:bb51658323e4927ca7669e9b439bfe21</token></loginResult>")

(defpage "/rest-service/auth-v1/login" {:keys [userName password]}
  (str mock-token))

(defpage "/rest-service/projects-v1" []
  (json/encode {:projects [{:projectData {:key "CR"}}
                           {:projectData {:key "Public"}}]} {:pretty true}))


(defn xml-comments [review-id]
  "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>
   <comments>
   <generalCommentData>
				<deleted>true</deleted>
        <message>foo</message>		
		    <replies/>
		</generalCommentData>
    <generalCommentData>
        <deleted>false</deleted>
        <message>general comment1</message>
				<replies/>
        <user>
            <userName>pertti</userName>
        </user>
    </generalCommentData>
    <generalCommentData>
        <deleted>false</deleted>
        <message>I thought you said you were going to remove this line</message>
				<replies>
					<generalCommentData>
						<deleted>false</deleted>
						<message>foo</message>		
						<replies/>
					</generalCommentData>
				</replies>
        <user>
            <userName>pertti</userName>
        </user>
    </generalCommentData>
    <versionedLineCommentData>
        <deleted>false</deleted>
        <message>some message</message>
				<replies>
					<generalCommentData>
						<deleted>false</deleted>
						<message>foo</message>		
						<replies/>
            <user>
              <userName>purtti</userName>
            </user>
					</generalCommentData>
				</replies>
        <user>
            <userName>purtti</userName>
        </user>
    </versionedLineCommentData>
    <versionedLineCommentData>
        <deleted>false</deleted>
        <message>some other message</message>        
        <user>
            <userName>pertti</userName>
        </user>
    </versionedLineCommentData>
  </comments>")

(defpage "/rest-service/reviews-v1/:id/comments" {:keys [id]}
  (xml-comments id))

(defn random-username []
  (str (rand-nth ["Arnold", "Jean Claude", "Sylvester"]) (rand-int 30)))

(defn random-project []
  (rand-nth ["CR", "EXERCISES", "FOO", "PUB"]))

(defn random-date []
  (str (+ 2006 (rand-int 6)) "-" 
       (format "%02d" (inc (rand-int 12))) "-" 
       (format "%02d" (inc (rand-int 28))) "T" 
       "12:00:00.000+1000"))

(defn random-mock-review []
  {:author {:userName (random-username)} 
            :createDate (random-date)
            :projectKey (random-project)})

(defn random-mock-reviews [count]
  (let [indexed-reviews (->> (repeatedly count random-mock-review)
                             (sort-by :projectKey)
                             (partition-by :projectKey)
                             (mapcat #(map-indexed vector %)))]
    (for [[idx review] indexed-reviews]
       (assoc review :permaId {:id (str (:projectKey review) "-" (+ 1 idx))}))))

(defn static-mock-reviews []
  [{:author {:userName "pertti"} :createDate "2012-01-15T15:59:49.855+1000" :permaId {:id "CR-1"} :projectKey "CR"}
   {:author {:userName "pertti"} :createDate "2012-01-16T15:59:49.855+1000" :permaId {:id "CR-2"} :projectKey "CR"}
   {:author {:userName "purtti"} :createDate "2012-02-15T15:59:49.855+1000" :permaId {:id "CR-3"} :projectKey "CR"}
   {:author {:userName "purtti"} :createDate "2012-02-16T15:59:49.855+1000" :permaId {:id "PUB-1"} :projectKey "PUB"}
   {:author {:userName "pertti"} :createDate "2012-03-15T15:59:49.855+1000" :permaId {:id "PUB-2"} :projectKey "PUB"}])

(defpage "/rest-service/reviews-v1.json" []
  (json/encode {:reviewData (random-mock-reviews 100)}))

