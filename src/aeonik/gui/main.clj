(ns aeonik.gui.main
  (:require [cljfx.api :as fx]
            [clojure.java.io :as io]
            [aeonik.gui.events :as events :refer [event-handler *state]])
  (:import (javafx.scene.image Image)
           [javafx.scene.input KeyCode KeyEvent]))

(defn title-input [{:keys [title]}]
  {:fx/type         :text-field
   :on-text-changed {:event/type :default}
   :text            title})

(defn directory-input [{:keys [current-dir]}]
  {:fx/type         :text-field
   :on-key-pressed  {:event/type ::events/set-current-dir}
   :text            current-dir})

(defn file-box [{:keys [filename]}]
  {:fx/type         :text-field
   :text            filename
   :on-key-pressed  {:event/type :default}
   :on-key-released {:event/type :default}})

(defn file-list [{:keys [files]}]
  {:fx/type        :h-box
   :editable       true
   :on-edit-commit {:event/type :default}
   :cell-factory   {:fx/cell-type :list-cell
                    :editable     true
                    :describe     (fn [item] {:text     item
                                              :editable true})}
   :items          files})

(defn file-list-view [{:keys [files]}]
  "This works"
  {:fx/type      :list-view
   :cell-factory {:fx/cell-type :list-cell
                  :describe     (fn [item] {:text item})}
   :items        files})

(defn graph-image [{:keys [image files]}]
  {:fx/type :image-view
   :image   {:is (io/input-stream image)}})

(defn root-view [{{:keys [title image files current-dir]} :state}]
  {:fx/type :stage
   :showing true
   :title   title
   :scene   {:fx/type :scene
             :root    {:fx/type  :v-box
                       :children [{:fx/type :label
                                   :text    "Window title input"}
                                  {:fx/type  :h-box
                                   :children [{:fx/type  :v-box
                                               :children [{:fx/type title-input
                                                           :title   title}
                                                          {:fx/type     directory-input
                                                           :current-dir (str current-dir)}
                                                          {:fx/type file-list-view
                                                           :files   files}
                                                          {:fx/type      :scroll-pane
                                                           :v-box/vgrow  :always
                                                           :fit-to-width true
                                                           :content      {:fx/type  :v-box
                                                                          :children (map
                                                                                      (fn [filename]
                                                                                        {:fx/type  file-box
                                                                                         :filename filename})
                                                                                      files)}}
                                                          ]}
                                              {:fx/type    :image-view ; Assuming you have :image-view to display images
                                               :image      image
                                               :fit-height 800
                                               :fit-width  800
                                               }]}]}}})

(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc (fn [state]
                                    {:fx/type root-view
                                     :state   state}))
    :opts {:fx.opt/map-event-handler event-handler}))

(fx/mount-renderer *state renderer)

(renderer)

(defn -main [& args]
  (fx/mount-renderer *state renderer))

(comment
  (do (require 'cljfx.dev)
      (cljfx.dev/help-ui)))