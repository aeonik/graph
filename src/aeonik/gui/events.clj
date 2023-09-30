(ns aeonik.gui.events
  (:require [cljfx.api :as fx]
            [aeonik.gui.subs :as subs]
            [clojure.java.io :as io]
            [babashka.fs :as fs])
  (:import [java.util UUID]
           (javafx.scene.image Image)
           [javafx.scene.input KeyCode KeyEvent]))

(def *state
  (atom {:title       "App title"
         :current-dir (fs/cwd)
         :image       (Image. (.toString (io/resource "2023-09-25T20:43:10.072745733-uber-graph.png")))
         :files       ["test" "more" "files" "here"]
         :graph       nil}))

(defn list-clojure-files [directory]
  "This works"
  (let [base-dir (fs/file directory)]
    (map #(str (fs/relativize base-dir %))
         (fs/glob base-dir "**/*.{clj,cljc}"))))

(defmulti event-handler :event/type)

(defmethod event-handler :default [event]
  (prn event))

(defmethod event-handler ::key-press-path [{:keys [fx/context ^KeyEvent fx/event]}]
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

(defmethod event-handler ::set-current-dir [event]
  "This works"
  (let [{fx-event :fx/event} event
        keycode (.getCode fx-event)]
    (when (= KeyCode/ENTER keycode)
      (let [source           (.getSource fx-event)
            text-field-value (.getText source)]
        (do
          (swap! *state assoc :current-dir text-field-value)
          (swap! *state assoc :files (list-clojure-files text-field-value)))))))

(defmethod event-handler ::display-files [{:keys [current-dir]}]
  (swap! *state assoc :files (list-clojure-files current-dir)))

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