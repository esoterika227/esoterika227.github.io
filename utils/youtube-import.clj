#!/bin/env bb
(ns esoterika227.youtube-import
  (:require [clojure.edn :as edn]
            [babashka.process :refer [shell]]
            [babashka.cli :as cli]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.data.csv :as csv]))

(defn parse-date-from-description [desc]
  (when desc
    (when-let [match (re-find #"(?i)(January|February|March|April|May|June|July|August|September|October|November|December)\s+\d{1,2}(?:st|nd|rd|th)?,?\s+\d{4}" desc)]
      (-> match
          (first)
          (str/replace #"(?:st|nd|rd|th)?,?" "")
          (java.time.LocalDate/parse (java.time.format.DateTimeFormatter/ofPattern "MMMM d yyyy"))))))

(defn parse-csv-row [headers row]
  (let [data (zipmap headers row)
        date (if (= "Unknown" (:date data))
               (parse-date-from-description (:description data))
               (:date data))]
    (assoc data :date (str date))))

(defn read-videos [filename]
  (with-open [reader (io/reader filename)]
    (let [csv-data (csv/read-csv reader)
          headers (mapv keyword (first csv-data))
          rows (rest csv-data)]
      (mapv (partial parse-csv-row headers) rows))))

(defn slugify [s]
  (-> s
      (str/lower-case)
      (str/replace #"[^a-z0-9]+" "-")
      (str/replace #"-$" "")))

(defn extract-youtube-id [url]
  (second (re-find #"(?:youtube\.com\/watch\?v=|youtu\.be\/)([^&\s]+)" url)))

(defn format-jekyll-post [video]
  (let [youtube-id (extract-youtube-id (:url video))]
    (str "---\n"
         "layout: video\n"
         "title: \"" (:title video) "\"\n"
         "excerpt: \"" (-> (:description video)
                          (str/replace #"\n" " ")
                          (str/replace #"\"" "\\\"")) "\"\n"
         "categories: videos\n"
         "tags: [lecture series, video]\n"
         "comments: true\n"
         "share: true\n"
         "youtube_id: " youtube-id "\n"
         "---\n\n"
         (:description video)
         "\n\n")))

(defn create-blog-post [output-dir video]
  (let [date (:date video)
        slug (slugify (:title video))
        filename (str date "-" slug ".md")
        filepath (io/file output-dir filename)]
    (io/make-parents filepath)
    (spit filepath (format-jekyll-post video))))

(defn create-blog-posts [output-dir videos]
  (doseq [video videos]
    (create-blog-post output-dir video)))

(defn -main [& args]
  (let [opts (cli/parse-opts args {:coerce {:file :string
                                           :output :string}
                                  :require [:file :output]})
        file (io/file (:file opts))
        output-dir (:output opts)]
    (cond
      (not (.exists file))
      (do
        (println "Error: File" (:file opts) "does not exist")
        (System/exit 1))
      
      (not (.canRead file))
      (do
        (println "Error: File" (:file opts) "is not readable")
        (System/exit 1))
      
      :else
      (let [videos (read-videos (:file opts))]
        (create-blog-posts output-dir videos)
        (println "Created" (count videos) "blog posts in" output-dir)))))

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
