(ns errors 
	(:use clojure.contrib.error-kit))
	


(deferror errorblock-appeared [] [msg page]
  {:msg msg
   :page page
   :unhandled (throw-msg IllegalArgumentException)})



(defn navigate-to [where] 
	(if (.startsWith where "notfound") 
		(raise errorblock-appeared "error block appeared" where)
		(println "navigate step 1 ok"))
	(println "navigate step 2 ok"))

(defn create-thing [what]
	(with-handler 
		(navigate-to what)
		(handle errorblock-appeared [msg page]
			(do (println (str "hit handler" page))
			  (if (= page "notfound-expected") 
					(do (println (str "hit expected error" page))
							(throw (Exception. "hit exp error")))
				  (do-not-handle))))))
							
