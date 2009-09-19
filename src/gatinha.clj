(ns gatinha 
	(:require [clojure.contrib.duck-streams :as duck-streams]
						[clojure.contrib.str-utils :as str-utils]))

(defn read-list [filename]
  (let [lines (duck-streams/read-lines filename)
 				list-lines (map #(vec (str-utils/re-split #"\," % 4)) lines)]
  	(into {} (map (fn [item] [(nth item 1) item]) list-lines))))
  		
(defn merge-lists [& files]
	(apply merge-with (fn [merged new] (into merged (let [[a _ c d] new] [a c d]))) (map read-list files)))
							
						
(defn write-csv [map file]	
	(duck-streams/write-lines file 
		(map (fn [item] (apply str (interpose "," item))) 
			(vals map))))