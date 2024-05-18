(ns aeonik.gui.events
  (:require [aeonik.util :as util]
            [cljfx.api :as fx]
            [aeonik.gui.subs :as subs]
            [clojure.java.io :as io]
            [babashka.fs :as fs]
            [aeonik.graph-no-classpath :as graph])
  (:import (java.io ByteArrayInputStream)
           [java.util UUID]
           (javafx.scene.image Image)
           [javafx.scene.input KeyCode KeyEvent]))

(def *state
  (atom {:title       "Graph Application"
         :current-dir (fs/cwd)
         :image       (util/str-path->jfx-image (io/resource "2023-09-25T20:43:10.072745733-uber-graph.png"))
         :files       (util/list-clojure-files (fs/cwd))
         :selection   [(first (util/list-clojure-files (fs/cwd)))]
         :graph       nil}))

(defmulti event-handler :event/type)

(defmethod event-handler :default [event]
  (prn event))

(defmethod event-handler ::key-press-path
  [{:keys [fx/context ^KeyEvent fx/event]}]
  "No idea what this is doing, got it from examples"
  (let [current-url (:url (fx/sub-ctx context subs/current-response))
        url         (fx/sub-val context :typed-url)]
    (when (and (= KeyCode/ENTER (.getCode event))
               (not= url current-url))
      {:dispatch {:event/type ::open-url :url url}})))

(defmethod event-handler ::set-title [{:keys [text]}]
  (swap! *state assoc :title text))

(defmethod event-handler ::set-image [e]
  (swap! *state assoc :image (:image e)))

(defmethod event-handler ::set-files [e]
  (swap! *state assoc :files (vec (:items e))))

(defmethod event-handler ::set-current-dir
  [event]
  "Handle the event when the user presses enter in the directory input field"
  (let [{fx-event :fx/event} event
        keycode (.getCode fx-event)]
    (when (= KeyCode/ENTER keycode)
      (let [source           (.getSource fx-event)
            text-field-value (.getText source)]
        (do
          (swap! *state assoc :current-dir text-field-value)
          (swap! *state assoc :files (util/list-clojure-files text-field-value)))))))

(defmethod event-handler ::display-files [{:keys [current-dir]}]
  (swap! *state assoc :files (util/list-clojure-files current-dir)))

(defmethod event-handler ::update-selected-files [event]
  (let [{selection :fx/event} event
        graph (graph/generate-multigraph selection)
        byte-array (graph/save-graph-image-to-byte-array graph)
        input-stream (ByteArrayInputStream. byte-array)]
    (do (swap! *state assoc :selection selection)
        (swap! *state assoc :graph graph)
        (swap! *state assoc :image (Image. input-stream)))))

(defmethod event-handler ::update-selected-files-svg [event]
  (let [{selection :fx/event} event
        graph (graph/generate-multigraph selection)
        byte-array (graph/save-graph-image-to-byte-array graph)
        input-stream (ByteArrayInputStream. byte-array)]
    (do (swap! *state assoc :selection selection)
        (swap! *state assoc :graph graph)
        (swap! *state assoc :image (graph/save-graph-svg-to-byte-array graph)))))

(defmethod event-handler ::set-filename [e]
  (clojure.pprint/pprint e)
  (swap! *state assoc :files (vec (:items e))))

(defmethod event-handler ::add-new-file [_]
  (swap! *state update :files conj ""))

(defmethod event-handler ::key-press [e]
  (cond
    (KeyCode/ENTER) (::set-filename e)
    (and (= KeyCode/N (.getCode e))
         (.isControlDown e)) (event-handler {::add-new-file {}})
    :else (clojure.pprint/pprint e)))
