(ns crucible_stats_facade.server
  (:require [noir.server :as server])
  (:gen-class))

(server/load-views-ns 'crucible_stats_facade.views)

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode
                        :ns 'crucible_stats_facade})))

