(ns traffic.core
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]
            [ring.util.codec :as ring]
            [cheshire.core :refer :all])
  (:use [clojure.data.zip.xml]))


(defn load-xml [in]
  (zip/xml-zip (xml/parse in)))
(def road-work (load-xml "/home/omer/Projects/clj/traffic/RoadWorkSet.xml"))
(def incident (load-xml "/home/omer/Projects/clj/traffic/IncidentSet.xml"))


(defn baz []
  (set (xml-> road-work :entry :title text)))

(defn inci []
  (xml-> incident :entry
         (fn [loc]
           {:lat (xml1-> loc :geo:lat text)
            :long (xml1-> loc :geo:long text)})))

(def json-spit (slurp "/home/omer/osxshare/spit.json"))

(defn parsed-spit []
  (map (fn [m]
         {:lat (:LATITUDE m)
          :long (:LONGITUDE m)})
       (:GPS_DATA (parse-string json-spit true))))

(defn make-query-string [m & [encoding]]
  (let [s #(if (instance? clojure.lang.Named %) (name %) %)
        enc (or encoding "UTF-8")]
    (->> (for [[k v] m]
           (str (ring/url-encode (s k) enc)
                "="
                (ring/url-encode (str v) enc)))
         (interpose "&")
         (apply str))))

(defn build-url [url-base query-map & [encoding]]
  (str url-base "?" (make-query-string query-map encoding)))

(defn ohyeah []
  (reduce (fn [acc x]
             (str acc "&markers=label:R%7C" (ring/url-encode x)))
           (build-url "maps.googleapis.com/maps/api/staticmap"
             {"center" "Singapore"
              "key" "AIzaSyBmkLA3tY_mZBq36oDBu98IJphpN3yUFxU"
              "sensor" "false"
              "size" "800x800"
              "zoom" "13"
              })
           (baz)))
(defn moar [chain]
  (reduce
   (fn [acc x]
     (str acc "&markers=label:I%7Ccolor:green%7C" (x :lat) "," (x :long)))
   chain
   (parsed-spit)
   ))

(defn -main [& args]
  (-> (ohyeah)
      (moar)
      (print)))
