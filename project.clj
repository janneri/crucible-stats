(defproject crucible_stats_facade "1.0"
            :description "Stats facade and UI for Crucible reviews"
            :dependencies [[org.clojure/clojure "1.4.0"]
                           [org.clojure/data.zip "0.1.0"]
                           [cheshire "4.0.0"]
                           [clj-http "0.5.5"]
                           [clj-time "0.4.4"]
                           [noir "1.3.0-beta3"]]
            :main crucible_stats_facade.server)

