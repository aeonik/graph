(ns build
  (:refer-clojure :exclude [test])
  (:require [clojure.tools.build.api :as b]))

(def lib 'net.clojars.aeonik/graph)
(def version "0.1.0-SNAPSHOT")
(def main 'aeonik.graph)
(def class-dir "target/classes")

(def basis (b/create-basis {}))

(defn test "Run all the tests." [opts]
  (let [basis    (b/create-basis {:aliases [:test]})
        cmds     (b/java-command
                  {:basis     basis
                   :main      'clojure.main
                   :main-args ["-m" "cognitect.test-runner"]})
        {:keys [exit]} (b/process cmds)]
    (when-not (zero? exit) (throw (ex-info "Tests failed" {}))))
  opts)

(defn- uber-opts [opts]
  (assoc opts
         :lib lib :main main
         :uber-file (format "target/%s-%s.jar" lib version)
         :basis (b/create-basis {})
         :class-dir class-dir
         :src-dirs ["src"]
         :ns-compile [main]))


;; Location of native libs: "~/.m2/repository/org/jogamp/jogl/jogl-all/2.3.2/jogl-all-2.3.2-" classifier ".jar"
(defn extract-native-libs []
  (let [basis (b/create-basis {})
        jar-path (-> basis
                     :libs
                     (get 'org.jogamp.gluegen/gluegen-rt$natives-linux-amd64)
                     :paths
                     first)
        jar-path2 (-> basis
                     :libs
                     (get 'org.jogamp.jogl/jogl-all$natives-linux-amd64)
                     :paths
                     first)
        dest-dir "natives"]
    (b/delete {:path dest-dir})
    (b/unzip {:zip-file jar-path2 :target-dir dest-dir})
    (b/unzip {:zip-file jar-path :target-dir dest-dir})))

(defn ci "Run the CI pipeline of tests (and build the uberjar)." [opts]
  (test opts)
  (b/delete {:path "target"})
  (let [opts (uber-opts opts)]
    (println "\nCopying source...")
    (b/copy-dir {:src-dirs ["resources" "src"] :target-dir class-dir})
    (println (str "\nCompiling " main "..."))
    (b/compile-clj opts)
    (println "\nBuilding JAR...")
    (b/uber opts))
  opts)
