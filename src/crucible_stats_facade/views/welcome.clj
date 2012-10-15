(ns crucible_stats_facade.views.welcome
  (:require [crucible_stats_facade.views.common :as common]
            [cheshire.core :as json]
            [noir.content.getting-started])
  (:use [noir.core :only [defpage]]))

(defpage "/welcome" []
         (common/layout
           [:p "Welcome to crucible_stats_facade xx2"]))
