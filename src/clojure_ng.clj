(ns clojure-ng)

(defn #^{:test {:groups #{:group1 :group2}}}
	test1
	[]
	(do (println "running test1")
	(println "test1 complete")))
	
(defn #^{:test {:groups #{:group2 :group3}}}
	test2
	[]
	(do(println "running test2")
	(println "test2 complete")))
	
(defn #^{:test {:groups #{:group2 :group3}}}
	test3
	[]
	(do (println "running test3")
	(throw (RuntimeException. "test failed!"))
	(println "test3 complete")))
	
(defn execute-test "returns either :pass, or an exception" [mytest]
	(try (do (mytest) :pass)
		(catch Exception e e))) 
		
(defn runalltests [metadatafilter]
  (let [tests (filter metadatafilter (vals (ns-publics *ns*)))]
  (apply hash-map 
  	(interleave tests
  		(map execute-test tests)))))


(defn in-group? [group myfn]
 (contains? (:groups (:test (meta myfn))) group))


	