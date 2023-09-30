(ns aeonik.meander-transforms
  (:require [clj-kondo.core :as clj-kondo]
            [meander.epsilon :as m]))


(defn analysis [paths]
  (clj-kondo/run! {:lint      paths
                   :config    {:analysis {:var-usages      true
                                          :var-definitions {:shallow false}}}
                   :skip-lint true}))

(defn namespace-defs [analysis]
  "Returns nodes of namespace definitions"
  [{:name ?name :as ?metadata}]

  [?name (dissoc ?metadata :name)])

(defn namespace-usages [analysis]
  "Returns adjacency list of namespace usages"
  (m/search analysis
            {:namespace-definitions (m/scan {:name ?name :as ?rest})
             :namespace-usages      (m/scan {:from ?name, :to ?to})}

            [?name ?rest ?to]))

(defn var-usages [analysis]
  "Returns adjacency list of var usages"
  (m/search (:analysis analysis)
            {:namespace-definitions (m/scan {:name ?ns :as ?rest})
             :var-usages            (m/scan {:from-var ?from
                                             :name     ?to
                                             :to       ?ns
                                             &         (m/and (m/guard (not= nil ?from))
                                                              (m/guard (not= nil ?to)))
                                             :as       ?var-rest})}


            [?from ?to ?var-rest]))

(def multigraph (apply uber/multidigraph
                       (var-usages analysis)))