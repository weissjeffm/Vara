(ns clojure-ng)
(def test? nil)

(defn #^{:test {:groups #{:group1 :group2} 
                :runBefore :test2}}
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
	
(defn execute-test "returns results, with the test name, and 
either :pass, :skip, or an exception, conj'd onto the end" 
	[mytest results]
	   (try (mytest) 
          :pass
     (catch Exception e e))) 
		
(defn run-tests-matching "Runs all tests using the testfilter-fn to filter
out any tests that shouldn't be run.  Returns a map of test fn's to their result."
 ([]
 (run-tests-matching test?))
 ([testfilter]
  (let [tests (filter 
                 testfilter 
                 (vals (ns-publics *ns*)))]
	  (loop [remaining-tests tests
           results []]
	  	(let [test (first remaining-tests)]
	  	  (if (empty? remaining-tests) results
	  	  (recur 
	  	    (rest remaining-tests) 
	  	    (conj results {test (execute-test test results)}))))))))

;  	(hash-map (map execute-test tests))))

(defn results-map [results]
	(into {} results))

(defn in-group? [group myfn]
 (contains? 
   (->(meta myfn) :test :groups) 
   group))

(defn test? [myfn]
	(contains? (meta myfn) :test))
	