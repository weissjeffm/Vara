(ns vara.api.hooks
  (:use
   vara.db.utils
   com.ashafa.clutch)
  (:refer-clojure :exclude [assert]))

(defn is-member? [id idseq]
  (let [group? (fn [thisid] (= (db-type thisid) :group))]
    (if (contains? (set idseq) id) true
	(if (some group? idseq) 
	  (recur id (let [groups (filter group? idseq)
			  first-exp (:members (with-db mydb (get-document (first groups))))
			  rest (rest groups)]
		      (into first-exp rest)))
	  false))))

(defn assert [exp message]
  (if-not exp message))

(defn validate-field "Validator functions should always take
these two arguments, and return a reason why the value is invalid, or nil if it's valid."
  [fn-str field value]
  (println *ns*)
  (if fn-str
    (let [validation-error (try ((eval (read-string fn-str)) field value) 
				(catch Exception e 
				  (throw (IllegalArgumentException.
					  (format "Invalid validator function: %s" fn-str) e))))]
      (if validation-error 
	(throw (Exception. (format "The field '%s' %s. Value given was %s" 
				   (:name field)
				   validation-error 
				   (str value))))))))
