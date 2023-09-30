(ns aeonik.graph-no-classpath
  (:require [clj-kondo.core :as clj-kondo]
            [clojure.set :as set]
            [meander.epsilon :as m]
            [ubergraph.core :as uber]
            [clojure.tools.cli :refer [parse-opts]]
            [ubergraph.alg :as alg])
  (:import (java.time LocalDateTime))
  (:gen-class))


(def paths ["/home/dave/Projects/joystick_fixer/src/aeonik/joystick_fixer/core.clj"])
(def analysis (clj-kondo/run! {:lint      paths
                               :config    {:analysis {:var-usages      true
                                                      :var-definitions {:shallow false}}}
                               :skip-lint true}))

(take 2 analysis)
(keys analysis)
(keys (:analysis analysis))
(:namespace-definitions (:analysis analysis))
(:namespace-usages (:analysis analysis))
(:var-definitions (:analysis analysis))

(take 1 (nthnext (:analysis analysis) 2))
(->> :var-definitions (:analysis analysis)
     :from-var)


(defn lift-name-up [input]
  (m/find input
          {:name ?name}
          ?name))

(def namespace-defs (:namespace-definitions (:analysis analysis)))
(def var-defs (:var-definitions (:analysis analysis)))



[{:name     ->evdev,
  :from-var -main}
 {:name     ->joydev,
  :from-var -main},
 {:name     defn
  :from-var nil}]
;; Basic meander search
(m/search namespace-defs
          [{:name ?name}] ?name)

;; Joining with meander? Scan works, not sure about the join logic
(m/search (:analysis analysis)

          {:namespace-definitions (m/scan {:name ?name})
           :var-definitions       (m/scan {:name ?var-name})}

          {:name     ?name
           :var-name ?var-name})

;; Trying to keep the rest of the metadata
(m/search namespace-defs
          [{:name ?name :as ?metadata}]

          [?name (dissoc ?metadata :name)])

;; Use this one for now
(m/search (:analysis analysis)
          {:namespace-definitions (m/scan {:name ?name :as ?rest})
           :namespace-usages      (m/scan {:from ?name, :to ?to})}

          [?name ?rest ?to])

;; Same as above but for vars, can't seem to get it to work because of nils, macros, etc...
(m/search (:analysis analysis)
          {:var-definitions (m/scan {:name ?name :as ?rest})
           :var-usages      (m/scan {:from-var ?from :name ?to :as ?var-rest})}

          [?from ?to ?var-rest])

;; Works!!!
(m/search (:analysis analysis)
          {:var-usages (m/scan {:from-var ?from
                                :name     ?to
                                &         (m/and (m/guard (not= nil ?from))
                                                 (m/guard (not= nil ?to)))
                                :as       ?var-rest})}


          [?from ?to ?var-rest])

(m/search clj-kondo-analysis
          {:var-usages (m/scan {:from-var ?from :name ?to})}

          [?from ?to])



;; Trying with rewrites and memory variables
(m/rewrites namespace-defs
            [{:name !name :as !rest}]
            [!name !rest])



;; Trying with vars
(m/rewrites var-defs
            [{:name !name :as !metadata} ...]

            [[!name !metadata] ...])

;; Dissoc name from metadata
(m/rewrites var-defs
            [{:name !name :as !metadata} ...]
            [[!name !metadata] ...])

(m/rewrites (:analysis analysis)
            {:var-usages [{:name !name :to !to :from-var !from} ...]
             &           (m/and (m/guard (not= nil !name))
                                (m/guard (not= nil !to))
                                (m/guard (not (m/re "clojure.core/.*" !to))))}

            [[!name !from !to] ...])

(lift-name-up (:namespace-definitions (:analysis analysis)))

(def nodes (map :name ({:keys [:namespace-definitions :namespace-usages]} analysis)))
(def edges (map (juxt :from :to) namespace-usages))

(defn var-usages [analysis]
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

(uber/pprint multigraph)

(uber/viz-graph multigraph
                {:save
                 {:filename (str "./" (LocalDateTime/now) "-uber-graph.svg")
                  :format   :svg}
                 :rankdir     "TB"
                 :nodesep     "1.0"
                 :ranksep     "3.0"
                 :splines     "polyline"
                 :dir         "forward"
                 :layout      :dot})