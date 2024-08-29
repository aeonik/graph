(ns aeonik.gui.main
  (:require [cljfx.api :as fx]
            [cljfx.ext.list-view :as fx.ext.list-view]
            [clojure.java.io :as io]
            [aeonik.gui.events :as events :refer [event-handler set-current-dir *state]]
            [aeonik.util :as util]
            [babashka.fs :as fs])
  (:import (java.io InputStream)
           (javafx.scene.image Image)
           [javafx.scene.input KeyCode KeyEvent]))

(defn title-input [{:keys [title]}]
  {:fx/type         :text-field
   :on-text-changed {:event/type :default}
   :text            title})

(defn directory-input [{:keys [current-dir]}]
  {:fx/type        :text-field
   :on-key-pressed {:event/type ::events/set-current-dir}
   :text           current-dir})

(defn file-box [{:keys [filename]}]
  {:fx/type         :text-field
   :text            filename
   :on-key-pressed  {:event/type :default}
   :on-key-released {:event/type :default}})

(defn file-list-view [{:keys [files selection current-dir]}]
  {:fx/type fx.ext.list-view/with-selection-props
   :props   {:selection-mode            :multiple
             :selected-items            selection
             :on-selected-items-changed {:event/type ::events/update-selected-files-svg}}
   :desc    {:fx/type      :list-view
             :cell-factory {:fx/cell-type :list-cell
                            :describe     (fn [item]
                                            {:text (util/str->relative-path item current-dir)})}
             :items        files}})


(defn graph-image [{:keys [image]}]
  {:fx/type :image-view
   :image   {:is (io/input-stream image)}})

(defn root-view [{{:keys [title image files current-dir selection]} :state}]
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
                                                          {:fx/type     file-list-view
                                                           :fit-to-width 300
                                                           :files       files
                                                           :selection   selection
                                                           :current-dir current-dir}
                                                          #_{:fx/type      :scroll-pane
                                                             :v-box/vgrow  :always
                                                             :fit-to-width true
                                                             :content      {:fx/type  :v-box
                                                                            :children (map
                                                                                        (fn [filename]
                                                                                          {:fx/type  file-box
                                                                                           :filename filename})
                                                                                        files)}}
                                                          ]}
                                              {:fx/type :scroll-pane
                                               :fit-to-width true
                                               :content
                                               {:fx/type    :image-view
                                                :image      image
                                                :smooth true
                                                :preserve-ratio  true
                                                }}]}]}}})

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
      (cljfx.dev/help-ui)
      (cljfx.dev/help :image-view)))
