(ns aeonik.quil.learning
  (:require [quil.core :as q]
            [quil.helpers.drawing :refer [line-join-points]]
            [quil.helpers.calc :refer [mul-add]]))

(defn setup []
  (q/frame-rate 1)                    ;; Set framerate to 1 FPS
  (q/background 200))                 ;; Set the background colour to
;; a nice shade of grey.
(defn draw []
  (q/stroke (q/random 255))             ;; Set the stroke colour to a random grey
  (q/stroke-weight (q/random 10))       ;; Set the stroke thickness randomly
  (q/fill (q/random 255))               ;; Set the fill colour to a random grey

  (let [diam (q/random 100)             ;; Set the diameter to a value between 0 and 100
        x    (q/random (q/width))       ;; Set the x coord randomly within the sketch
        y    (q/random (q/height))]     ;; Set the y coord randomly within the sketch
    (q/ellipse x y diam diam)))         ;; Draw a circle at x y with the correct diameter

(q/defsketch example                  ;; Define a new sketch named example
             :title "Oh so many grey circles"    ;; Set the title of the sketch
             :settings #(q/smooth 2)             ;; Turn on anti-aliasing
             :setup setup                        ;; Specify the setup fn
             :draw draw                          ;; Specify the draw fn
             :size [323 200])                    ;; You struggle to beat the golden ratio


(def radius 100)

(defn setup []
  (q/background 255)
  (q/stroke 00))

(defn draw []
  (q/background 255)
  (q/translate (/ (q/width) 2) (/ (q/height) 2) 0)
  (q/rotate-y (* (q/frame-count) 0.03))
  (q/rotate-x (* (q/frame-count) 0.04))
  (let [line-args (for [t (range 0 180)]
                    (let [s        (* t 18)
                          radian-s (q/radians s)
                          radian-t (q/radians t)
                          x (* radius  (q/cos radian-s) (q/sin radian-t))
                          y (* radius  (q/sin radian-s) (q/sin radian-t))
                          z (* radius (q/cos radian-t))]
                      [x y z]))]
    (dorun
      (map #(apply q/line %) (line-join-points line-args)))))


(q/defsketch gen-art-29
           :title "Spiral Sphere"
           :setup setup
           :draw draw
           :size [500 300]
           :renderer :opengl)
