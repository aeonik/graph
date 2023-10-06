(ns aeonik.membrane.main
  (:require [membrane.java2d :as java2d]
                                  [membrane.ui :as ui
                                   :refer [vertical-layout
                                           translate
                                           horizontal-layout
                                           button
                                           label
                                           with-color
                                           bounds
                                           spacer
                                           on]]
                                  [membrane.component :as component
                                   :refer [defui defeffect]]
                                  [membrane.basic-components :as basic]))

(java2d/run #(ui/label "Hello World!"))

(ui/label "Hello\nWorld!")
(ui/label "Hello\nWorld!" (ui/font "Menlo" 22))
(ui/label "Hello\nWorld!" (ui/font nil 22))

(ui/path [24.20 177.98]
         [199.82 37.93]
         [102.36 240.31]
         [102.36 15.68]
         [199.82 218.06]
         [24.20 78.01]
         [243.2 127.99]
         [24.20 177.98])

(def app-state (atom false))

(defui checkbox [ {:keys [checked?]}]
       (on
         :mouse-down
         (fn [_]
           [[::toggle $checked?]])
         (ui/label (if checked?
                     "X"
                     "O"))))

(defeffect ::toggle [$checked?]
           (dispatch! :update $checked? not))

(defui checkbox-test [{:keys [x y z]}]
       (vertical-layout
         (checkbox {:checked? x})
         (checkbox {:checked? y})
         (checkbox {:checked? z})
         (ui/label
           (with-out-str (clojure.pprint/pprint
                           {:x x
                            :y y
                            :z z})))))

(comment (java2d/run (component/make-app #'checkbox-test {:x false :y true :z false})))

(defui item-row [ {:keys [item-name selected?]}]
       (on
         :mouse-down
         (fn [_]
           [[:update $selected? not]])
         ;; put the items side by side
         (horizontal-layout
           (translate 5 5
                      ;; checkbox in `membrane.ui` is non interactive.
                      (ui/checkbox selected?))
           (spacer 5 0)
           (ui/label item-name))))

(comment
  ;; It's a very common workflow to work on sub components one piece at a time.
  (java2d/run (component/make-app #'item-row {:item-name "my item" :selected? false})))

(defui item-selector
       "`item-names` a vector of choices to select from
     `selected` a set of selected items
     `str-filter` filter out item names that don't contain a case insensitive match for `str-filter` as a substring
     "
       [{:keys [item-names selected str-filter]
         :or {str-filter ""
              selected #{}}}]
       (let [filtered-items (filter #(clojure.string/includes? (clojure.string/lower-case %) str-filter) item-names)]
         (apply
           vertical-layout
           (basic/textarea {:text str-filter})
           (for [iname filtered-items]
             ;; override the default behaviour of updating the `selected?` value directly
             ;; instead, we'll keep the list of selected items in a set
             (on :update
                 (fn [& args]
                   [[:update $selected (fn [selected]
                                         (if (contains? selected iname)
                                           (disj selected iname)
                                           (conj selected iname)))]])
                 (item-row {:item-name iname :selected? (get selected iname)}))))))

(comment
  (java2d/run (component/make-app #'item-selector {:item-names (->> (clojure.java.io/file ".")
                                                                    (.listFiles)
                                                                    (map #(.getName %)))} )))

(defn file-selector [path]
  (let [state (atom {:item-names
                     (->> (clojure.java.io/file path)
                          (.listFiles)
                          (map #(.getName %))
                          sort)})]
    (java2d/run-sync (component/make-app #'item-selector state))
    (:selected @state)))