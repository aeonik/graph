(ns aeonik.graph
  (:refer-clojure :exclude [parse-opts])
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
  )

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (if (or (seq errors) (empty? arguments))
      (println "Usage:" summary)
      (generate-graph arguments (:regex options) (:node-remove options)))))

