(ns aeonik.old-graph
  (:require [thomasa.morpheus.core :as m]
            [clojure.set :as set]
            [ubergraph.core :as uber]
            [clojure.tools.cli :refer [parse-opts]]
            [ubergraph.alg :as alg])
  (:import (java.time LocalDateTime))
  (:gen-class))

(def cli-options
  [["-r" "--regex REGEX" "Regex pattern to exclude" :default "clojure.core/.*|:clj-kondo/unknown-namespace/.*"]
   ["-n" "--node-remove NODE" "Node to remove from the graph" :default nil]])

(defn generate-graph [files regex node-to-remove]
  (let [arguments files
        exclude-regexp (re-pattern regex)
        analysis (m/lint-analysis arguments)
        graph (m/var-deps-graph analysis nil exclude-regexp)
        all-internal-vars (m/->vars analysis exclude-regexp)
        uber-graph (uber/multidigraph (:adj graph))
        new-graph (uber/remove-nodes uber-graph node-to-remove)]

    (uber/viz-graph new-graph
                    {:save
                     {:filename (str "./" (LocalDateTime/now) "-uber-graph.svg")
                      :format   :svg}
                     :rankdir "TB"
                     :nodesep "1.0"
                     :ranksep "3.0"
                     :splines "polyline"
                     :dir "forward"
                     :layout  :dot})))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (if (or (seq errors) (empty? arguments))
      (println "Usage:" summary)
      (generate-graph arguments (:regex options) (:node-remove options)))))

(ns aeonik.old-graph)
