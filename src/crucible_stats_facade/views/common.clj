(ns crucible_stats_facade.views.common
  (:use [noir.core :only [defpartial]]
        [hiccup.page :only [include-css html5]]))

(defpartial layout [& content]
            (html5
              [:head
               [:title "crucible_stats_facade"]
               (include-css "/css/reset.css")]
              [:body
               [:div#wrapper
                content]]))
