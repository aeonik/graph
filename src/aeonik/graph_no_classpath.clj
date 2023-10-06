(ns aeonik.graph-no-classpath
  (:require [clj-kondo.core :as clj-kondo]
            [aeonik.util :as util]
            [clojure.string :as str]
            [meander.epsilon :as m]
            [ubergraph.core :as uber]
            [loom.alg :as loom]
            [dorothy.jvm :as dj]
            [dorothy.core :as d])
  (:import (java.io ByteArrayOutputStream)
           (java.time LocalDateTime))
  (:gen-class))

(defn analysis [paths]
  (clj-kondo/run! {:lint      paths
                   :config    {:analysis {:var-usages      true
                                          :var-definitions {:shallow false}}}
                   :skip-lint true}))

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

(defn generate-multigraph [paths]
  (apply uber/multidigraph
         (var-usages (analysis paths))))

(defn save-graph-image-to-byte-array [graph]
  (let [byte-output-stream (ByteArrayOutputStream.)]
    (uber/viz-graph graph
                    {:save
                     {:filename byte-output-stream
                      :format   :png}
                     :rankdir     "TB"
                     :nodesep     "1.0"
                     :ranksep     "3.0"
                     :splines     "polyline"
                     :dir         "forward"
                     :concentrate "true"
                     :layout      :dot})
    (.toByteArray byte-output-stream)))

(defn generate-graph-svg-to-byte-array [graph]
  (let [byte-output-stream (ByteArrayOutputStream.)]
    (uber/viz-graph graph
                    {:save
                     {:filename byte-output-stream
                      :format   :svg}
                     :rankdir     "TB"
                     :nodesep     "1.0"
                     :ranksep     "3.0"
                     :splines     "polyline"
                     :dir         "forward"
                     :concentrate "true"
                     :layout      :dot
                     #_#_:layout :neato
                     #_#_:layout :circo
                     #_#_:layoug :fdp
                     #_#_:layout :sfdp
                     #_#_:layout :osage
                     #_#_:layout :patchwork
                     #_#_:layout :twopi})
    (.toByteArray byte-output-stream)))

(defn generate-graph-image [graph]
  (uber/viz-graph graph
                  {:save
                   {:filename (str "./" (LocalDateTime/now) "-uber-graph.png")
                    :format   :png}
                   :rankdir     "TB"
                   :nodesep     "1.0"
                   :ranksep     "3.0"
                   :splines     "polyline"
                   :dir         "forward"
                   :concentrate "true"
                   #_#_:layout      :dot
                   :layout :circo
                   }))

(defn escape-id [id]
  (let [id-str (str id)  ; Convert to string
        safe-id-pattern #"^[_a-zA-Z\u0080-\u0255][_a-zA-Z0-9\u0080-\u0255]*$"]
    (if (re-find safe-id-pattern id-str)
      id-str
      (str "\"" (clojure.string/replace id-str "\"" "\\\"") "\""))))

(defn uber-to-dorothy
  "Converts an Ubergraph to a Dorothy-compatible Dot string"
  [g]
  (let [edges (map (fn [e]
                     (str "  " (escape-id (uber/src e)) " -> " (escape-id (uber/dest e)) ";"))
                   (uber/edges g))]
    (str "digraph {\n"
         (clojure.string/join "\n" edges)
         "\n}")))
(defn save-graph-svg-to-byte-array [graph]
  #_(-> graph
      uber-to-dorothy
      #_d/dot
      (dj/render {:format :svg})
      util/svg->jfx-image)
  (-> graph
      generate-graph-svg-to-byte-array
      util/byte-array->svg-string))


#_(comment
    (def paths ["/home/dave/Projects/joystick_fixer/src/aeonik/joystick_fixer/core.clj"])
    (take 2 (analysis paths))
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
    (def multigraph (apply uber/multidigraph
                           (var-usages (analysis paths))))

    (uber/pprint multigraph)
    multigraph

    (keys multigraph)
    (take 1 (:node-map multigraph))
    (take 2 (:node-map multigraph))
    (map :out-degree (:node-map multigraph))
    (uber/nodes multigraph)
    (uber/traverse multigraph)
    (loom/pre-traverse multigraph)

    (-> (d/digraph [[:a :b :c] [:b :c]])
        d/dot
        #_(dj/render {:format :svg }))
    (-> (uber/multidigraph [[:a :b] [:b :c]])
        uber-to-dorothy
        d/dot
        #_(dj/render {:format :svg }))
    (-> multigraph
         uber-to-dorothy
         d/dot
         #_(dj/render {:format :svg}))
    (-> multigraph
        uber/edges)

    ()


    (uber/viz-graph multigraph
                    {:save
                     {:filename (str "./" (LocalDateTime/now) "-uber-graph.svg")
                      :format   :svg}
                     :rankdir "TB"
                     :nodesep "1.0"
                     :ranksep "3.0"
                     :splines "polyline"
                     :dir     "forward"
                     :layout  :dot}))

