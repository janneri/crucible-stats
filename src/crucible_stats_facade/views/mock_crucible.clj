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

(defn mock-comments [review-id]
  {:comments [{:generalCommentData {:message (str review-id " generalcomment") :user {:userName "pertti"}}}
              {:versionedLineCommentData {:message (str review-id " comment") :user {:userName "purtti"}}}]})

(defpage "/rest-service/reviews-v1/:id/comments" {:keys [id]}
         (json/encode (mock-comments id)))

(defn mock-reviews []
  {:reviews [{:reviewData {:author {:userName "pertti1"} :createDate "2012-01-15T15:59:49.855+1000" :permaId {:id "CR-1"} :projectKey "CR"}}
             {:reviewData {:author {:userName "pertti2"} :createDate "2012-01-16T15:59:49.855+1000" :permaId {:id "CR-2"} :projectKey "CR"}}
             {:reviewData {:author {:userName "pertti3"} :createDate "2012-02-15T15:59:49.855+1000" :permaId {:id "CR-3"} :projectKey "CR"}}
             {:reviewData {:author {:userName "pertti4"} :createDate "2012-02-16T15:59:49.855+1000" :permaId {:id "PUB-1"} :projectKey "PUB"}}
             {:reviewData {:author {:userName "pertti5"} :createDate "2012-03-15T15:59:49.855+1000" :permaId {:id "PUB-2"} :projectKey "PUB"}}]})

(defpage "/rest-service/reviews-v1" []
         (json/encode (mock-reviews)))

