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

(defn mock-reviews []
  {:reviews [{:reviewData {:author {:userName "pertti"} :createDate "2012-01-15T15:59:49.855+1000" :permaId {:id "CR-1"} :projectKey "CR"}}
             {:reviewData {:author {:userName "pertti"} :createDate "2012-01-16T15:59:49.855+1000" :permaId {:id "CR-2"} :projectKey "CR"}}
             {:reviewData {:author {:userName "purtti"} :createDate "2012-02-15T15:59:49.855+1000" :permaId {:id "CR-3"} :projectKey "CR"}}
             {:reviewData {:author {:userName "purtti"} :createDate "2012-02-16T15:59:49.855+1000" :permaId {:id "PUB-1"} :projectKey "PUB"}}
             {:reviewData {:author {:userName "pertti"} :createDate "2012-03-15T15:59:49.855+1000" :permaId {:id "PUB-2"} :projectKey "PUB"}}]})

(defpage "/rest-service/reviews-v1" []
         (json/encode (mock-reviews)))

