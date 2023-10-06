(ns aeonik.util
  (:require [babashka.fs :as fs]
            [clojure.java.io :as io]
            [dorothy.core :as d]
            [dorothy.jvm :as dj]
            [ubergraph.core :as uber])
  (:import (java.nio.file Path)
           (javafx.scene.image Image)
           (org.girod.javafx.svgimage SVGImage
                                      SVGLoader)))

(defn str->relative-path [^String str-path ^Path current-dir]
  (-> (fs/path current-dir)
      (fs/relativize (fs/path str-path))
      (str)))

(defn str-path->jfx-image [str-path]
  (Image. (.toString str-path)))

(defn list-clojure-files [^String directory]
  "List all Clojure files with relative paths from the given directory as a vector of strings"
  (let [base-dir (fs/file directory)]
    (map #(str (fs/absolutize %))
         (fs/glob base-dir "**/*.{clj,cljc}"))))

(defn svg->jfx-image-scaled [svg quality scale-x scale-y]
  (let [svg-image (SVGLoader/load svg)]
    (.toImageScaled svg-image quality scale-x scale-y)))

(defn svg->jfx-image [svg]
  (-> svg
      SVGLoader/load
      (.toImageScaled 1)))

(defn byte-array->svg-string [^bytes byte-array]
  (-> byte-array
      (String. "UTF-8")
      svg->jfx-image))
