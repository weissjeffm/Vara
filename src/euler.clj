;; euler
(def prime?)

(def primes 
	(lazy-seq (concat [2 3 5] (filter prime? (iterate #(+ 2 %) 7)))))
	
(defn prime? [mynum]
	(every? #(> (rem mynum %) 0) 
	        (take-while #(<= %  (. Math sqrt mynum))  primes)))
	
(defn rotations [n]
	(let [len  (count (str n))]
		(map #(Long/parseLong %) (rest (take len (iterate left-circular n))))))
		
(defn left-circular [n]
	(let [strnum (str n)]
		 (apply str (concat (rest strnum) [(first strnum)]))))

(defn circular? [n]
	(every? prime? (rotations n)))
	
(defn solve-35 []
	(count (filter circular? (take-while #(< % 1000000) primes))))
	
(defn mysequence [n]
	(lazy-seq 
		(concat 
			(take-while #(not= % 1) 
				(iterate 
					(fn [x] 
						(if (even? x) (/ x 2) 
							(inc (* 3 x))))
					 n))
			[1])))
			
(defn solve-14 "outer fn for euler #14" 
	(reduce 
		(fn [hasha hashb] 
			(if (> (:value hasha) (:value hashb)) hasha hashb)) 
		(map (fn [x] {:index x :value (count (mysequence x))}) (take-while #(<= % 1000000) (iterate inc 1)))))
		

			